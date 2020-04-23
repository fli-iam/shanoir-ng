package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.ng.exchange.model.ExExamination;
import org.shanoir.ng.exchange.model.ExStudy;
import org.shanoir.ng.exchange.model.ExStudyCard;
import org.shanoir.ng.exchange.model.ExSubject;
import org.shanoir.ng.exchange.model.Exchange;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.PseudonymusHashValues;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.utils.Util;

/**
 * This class implements the logic when the start import button is clicked.
 * 
 * @author mkain
 * 
 */
public class ImportFinishActionListenerNG implements ActionListener {

	private static Logger logger = Logger.getLogger(ImportFinishActionListenerNG.class);

	private MainWindow mainWindow;
	
	private UploadJob uploadJob;
	
	private File uploadFolder;
	
	private Subject subject;
	
	private ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;
	
	private ImportStudyAndStudyCardCBItemListenerNG importStudyAndStudyCardCBILNG;

	public ImportFinishActionListenerNG(final MainWindow mainWindow, UploadJob uploadJob, File uploadFolder, Subject subject,
			ImportStudyAndStudyCardCBItemListenerNG importStudyAndStudyCardCBILNG) {
		this.mainWindow = mainWindow;
		this.uploadJob = uploadJob;
		this.uploadFolder = uploadFolder;
		this.subject = subject;
		this.importStudyAndStudyCardCBILNG = importStudyAndStudyCardCBILNG;
		this.shanoirUploaderServiceClientNG = ShUpOnloadConfig.getShanoirUploaderServiceClientNG();
	}

	/**
	 * This method contains all the logic which is performed when the start import
	 * button is clicked.
	 */
	public void actionPerformed(final ActionEvent event) {
		final Study study = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
		final StudyCard studyCard = (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem();
		if (study == null || study.getId() == null || studyCard == null || studyCard.getName() == null) {
			return;
		}
		
		// block further action
		((JButton) event.getSource()).setEnabled(false);
		mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		/**
		 * Handle subject here: creation or use existing
		 */
		if (subject == null) {
			try {
				 subject = fillSubject(mainWindow.importDialog, uploadJob);
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			}
			subject = shanoirUploaderServiceClientNG.createSubject(subject, ShUpConfig.isModeSubjectCommonNameManual(), studyCard.getCenterId());
			if (subject == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			} else {
				handleSubjectStudy(study, subject);
				logger.info("Auto-Import: subject created on server with ID: " + subject.getId());
			}
		} else {
			handleSubjectStudy(study, subject);
			logger.info("Auto-Import: subject used on server with ID: " + subject.getId());
		}
		
		Long examinationId = null;
		if (mainWindow.importDialog.mrExaminationNewExamCB.isSelected()) {
			Examination examinationDTO = new Examination();
			IdName studyIdName = new IdName(study.getId(), study.getName());
			examinationDTO.setStudy(studyIdName);
			IdName subjectIdName = new IdName(subject.getId(), subject.getName());
			examinationDTO.setSubject(subjectIdName);
			IdName center = (IdName) mainWindow.importDialog.mrExaminationCenterCB.getSelectedItem();
//			Investigator investigator = (Investigator) mainWindow.importDialog.mrExaminationExamExecutiveCB.getSelectedItem();
			Date examinationDate = (Date) mainWindow.importDialog.mrExaminationDateDP.getModel().getValue();
			String examinationComment = mainWindow.importDialog.mrExaminationCommentTF.getText();
			IdName centerIdName = new IdName(center.getId(), center.getName());
			examinationDTO.setCenter(centerIdName);
			examinationDTO.setExaminationDate(examinationDate);
			examinationDTO.setComment(examinationComment);

			/**
			 * TODO handle investigators here or decide finally to delete them in sh-ng
			 */
			examinationDTO = shanoirUploaderServiceClientNG.createExamination(examinationDTO);
			if (examinationDTO == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.createmrexamination"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			} else {
				examinationId = examinationDTO.getId();
				logger.info("Auto-Import: examination created on server with ID: " + examinationId);
			}
		} else {
			Examination examinationDTO = (Examination) mainWindow.importDialog.mrExaminationExistingExamCB.getSelectedItem();
			examinationId = examinationDTO.getId();
			logger.info("Auto-Import: examination used on server with ID: " + examinationId);
		}
				
		/**
		 * 3. Fill exchange.json
		 */
		Exchange exchange = prepareExchange(mainWindow.importDialog, subject.getName(), subject.getId(), examinationId);
		Runnable runnable = new ImportFinishRunnableNG(uploadJob, uploadFolder, exchange, subject.getName());
		Thread thread = new Thread(runnable);
		thread.start();
		
		mainWindow.importDialog.setVisible(false);
		mainWindow.setCursor(null); // turn off the wait cursor
		((JButton) event.getSource()).setEnabled(true);
		
		JOptionPane.showMessageDialog(mainWindow.frame,
				ShUpConfig.resourceBundle.getString("shanoir.uploader.import.start.auto.import.message"),
				"Import", JOptionPane.INFORMATION_MESSAGE);
	}

	private void handleSubjectStudy(final Study study, final Subject subject) {
		if (importStudyAndStudyCardCBILNG.getSubjectStudy() == null) {
			SubjectStudy subjectStudy = new SubjectStudy();
			subjectStudy.setStudy(new IdName(study.getId(), study.getName()));
			subjectStudy.setSubject(new IdName(subject.getId(), subject.getName()));
			subjectStudy.setSubjectStudyIdentifier(mainWindow.importDialog.subjectStudyIdentifierTF.getText());
			subjectStudy.setSubjectType((SubjectType) mainWindow.importDialog.subjectTypeCB.getSelectedItem());
			subjectStudy.setPhysicallyInvolved(mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.isSelected());
			subject.getSubjectStudyList().add(subjectStudy);
			shanoirUploaderServiceClientNG.createSubjectStudy(subject);
			logger.info("Auto-import: RelSubjectStudy created with id: " + subjectStudy.getId() + " for subject: " + subject.getName() + " in study: " + study.getName());
		}
	}

	/**
	 * 
	 * @param importDialog
	 * @param dicomData
	 * @return
	 * @throws ParseException 
	 */
	private Subject fillSubject(final ImportDialog importDialog, final UploadJob uploadJob) throws ParseException {
		final Subject subjectDTO = new Subject();
		/**
		 * Values coming from UploadJob
		 */
		subjectDTO.setIdentifier(uploadJob.getSubjectIdentifier());
        Date birthDate = ShUpConfig.formatter.parse(uploadJob.getPatientBirthDate());
		subjectDTO.setBirthDate(Util.convertToLocalDateViaInstant(birthDate));
		if (uploadJob.getPatientSex().compareTo(Sex.F.name()) == 0) {
			subjectDTO.setSex(Sex.F);
		} else if (uploadJob.getPatientSex().compareTo(Sex.M.name()) == 0) {
			subjectDTO.setSex(Sex.M);
		}
		if (ShUpConfig.isModePseudonymus()) {
			fillPseudonymusHashValues(uploadJob, subjectDTO);
		}
		/**
		 * Values coming from ImportDialog
		 */
		if (ShUpConfig.isModeSubjectCommonNameManual()) {
			subjectDTO.setName(importDialog.subjectTextField.getText());
		}		
		subjectDTO.setImagedObjectCategory((ImagedObjectCategory) importDialog.subjectImageObjectCategoryCB.getSelectedItem());
		String languageHemDom = (String) importDialog.subjectLanguageHemisphericDominanceCB.getSelectedItem();
		if (HemisphericDominance.Left.getName().compareTo(languageHemDom) == 0) {
			subjectDTO.setLanguageHemisphericDominance(HemisphericDominance.Left);
		} else if (HemisphericDominance.Right.getName().compareTo(languageHemDom) == 0) {
			subjectDTO.setLanguageHemisphericDominance(HemisphericDominance.Right);
		}
		String manualHemDom = (String) importDialog.subjectManualHemisphericDominanceCB.getSelectedItem();
		if (HemisphericDominance.Left.getName().compareTo(manualHemDom) == 0) {
			subjectDTO.setManualHemisphericDominance(HemisphericDominance.Left);
		} else if (HemisphericDominance.Right.getName().compareTo(manualHemDom) == 0) {
			subjectDTO.setManualHemisphericDominance(HemisphericDominance.Right);
		}
		return subjectDTO;
	}

	/**
	 * @param dicomData
	 * @param subjectDTO
	 */
	private void fillPseudonymusHashValues(final UploadJob uploadJob, final Subject subjectDTO) {
		PseudonymusHashValues pseudonymusHashValues = new PseudonymusHashValues();
		pseudonymusHashValues.setFirstNameHash1(uploadJob.getFirstNameHash1());
		pseudonymusHashValues.setFirstNameHash2(uploadJob.getFirstNameHash2());
		pseudonymusHashValues.setFirstNameHash3(uploadJob.getFirstNameHash3());
		pseudonymusHashValues.setLastNameHash1(uploadJob.getLastNameHash1());
		pseudonymusHashValues.setLastNameHash2(uploadJob.getLastNameHash2());
		pseudonymusHashValues.setLastNameHash3(uploadJob.getLastNameHash3());
		pseudonymusHashValues.setBirthNameHash1(uploadJob.getBirthNameHash1());
		pseudonymusHashValues.setBirthNameHash2(uploadJob.getBirthNameHash2());
		pseudonymusHashValues.setBirthNameHash3(uploadJob.getBirthNameHash3());
		pseudonymusHashValues.setBirthDateHash(uploadJob.getBirthDateHash());
		subjectDTO.setPseudonymusHashValues(pseudonymusHashValues);
	}

	private Exchange prepareExchange(ImportDialog importDialog, String subjectName, Long subjectId, Long examinationId) {
		Exchange exchange = new Exchange();
		// Study
		Study study = (Study) importDialog.studyCB.getSelectedItem();
		ExStudy exStudy = new ExStudy();
		exStudy.setStudyName(study.getName());
		exStudy.setStudyId(study.getId());
		exchange.setExStudy(exStudy);
		// StudyCard
		StudyCard studyCard = (StudyCard) importDialog.studyCardCB.getSelectedItem();
		ExStudyCard exStudyCard = new ExStudyCard();
		exStudyCard.setName(studyCard.getName());
		ArrayList<ExStudyCard> exStudyCards = new ArrayList<ExStudyCard>();
		exStudyCards.add(exStudyCard);
		exStudy.setExStudyCards(exStudyCards);
		// Subject
		ExSubject exSubject = new ExSubject();
		exSubject.setSubjectName(subjectName);
		exSubject.setSubjectId(subjectId);
		ArrayList<ExSubject> exSubjects = new ArrayList<ExSubject>();
		exSubjects.add(exSubject);
		exStudy.setExSubjects(exSubjects);
		// Examination
		ExExamination exExamination = new ExExamination();
		exExamination.setId(examinationId);
		ArrayList<ExExamination> exExaminations = new ArrayList<ExExamination>();
		exExaminations.add(exExamination);
		exSubject.setExExaminations(exExaminations);
		return exchange;
	}

}
