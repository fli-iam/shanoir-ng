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
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;

/**
 * This class implements the logic when the start import button is clicked.
 * 
 * @author mkain
 * 
 */
public class ImportFinishActionListener implements ActionListener {

	private static Logger logger = Logger.getLogger(ImportFinishActionListener.class);

	private MainWindow mainWindow;
	
	private UploadJob uploadJob;
	
	private File uploadFolder;
	
	private Subject subject;
	
	private ShanoirUploaderServiceClient shanoirUploaderServiceClient;
	
	private ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBILNG;

	public ImportFinishActionListener(final MainWindow mainWindow, UploadJob uploadJob, File uploadFolder, Subject subject,
			ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBILNG) {
		this.mainWindow = mainWindow;
		this.uploadJob = uploadJob;
		this.uploadFolder = uploadFolder;
		this.subject = subject;
		this.importStudyAndStudyCardCBILNG = importStudyAndStudyCardCBILNG;
		this.shanoirUploaderServiceClient = ShUpOnloadConfig.getShanoirUploaderServiceClient();
	}

	/**
	 * This method contains all the logic which is performed when the start import
	 * button is clicked.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final Study study = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
		final StudyCard studyCard = (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem();
		if (study == null || study.getId() == null || studyCard == null || studyCard.getName() == null) {
			return;
		}
		if (ShUpConfig.isModeSubjectCommonNameManual()) {
			// minimal length for subject common name is 2, same for subject study identifier
			if (mainWindow.importDialog.subjectTextField.getText().length() < 2
				|| !mainWindow.importDialog.subjectStudyIdentifierTF.getText().isEmpty()
						&& mainWindow.importDialog.subjectStudyIdentifierTF.getText().length() < 2) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.subject.creation"),
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
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
			ImportUtils.addSubjectStudy(study, subject,
					(SubjectType) mainWindow.importDialog.subjectTypeCB.getSelectedItem(),
					mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.isSelected(),
					mainWindow.importDialog.subjectStudyIdentifierTF.getText());
			// create subject with subject-study filled to avoid access denied exception because of rights check
			Long centerId = studyCard.getAcquisitionEquipment().getCenter().getId();
			subject = shanoirUploaderServiceClient.createSubject(subject, ShUpConfig.isModeSubjectCommonNameManual(), centerId);
			if (subject == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			} else {
				logger.info("Auto-Import: subject created on server with ID: " + subject.getId());
			}
		} else {
			// if rel-subject-study does not exist for existing subject, create one
			if (importStudyAndStudyCardCBILNG.getSubjectStudy() == null) {
				ImportUtils.addSubjectStudy(study, subject,
						(SubjectType) mainWindow.importDialog.subjectTypeCB.getSelectedItem(),
						mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.isSelected(),
						mainWindow.importDialog.subjectStudyIdentifierTF.getText());
				if (shanoirUploaderServiceClient.createSubjectStudy(subject) == null) {
					JOptionPane.showMessageDialog(mainWindow.frame,
							mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
							"Error", JOptionPane.ERROR_MESSAGE);
					mainWindow.setCursor(null); // turn off the wait cursor
					((JButton) event.getSource()).setEnabled(true);
					return;
				}
				
			}
			logger.info("Auto-Import: subject used on server with ID: " + subject.getId());
		}
		
		Long examinationId = null;
		if (mainWindow.importDialog.mrExaminationNewExamCB.isSelected()) {
			Examination examinationDTO = new Examination();
			examinationDTO.setStudyId(study.getId());
			examinationDTO.setSubjectId(subject.getId());
			IdName center = (IdName) mainWindow.importDialog.mrExaminationCenterCB.getSelectedItem();
			examinationDTO.setCenterId(center.getId());
//			Investigator investigator = (Investigator) mainWindow.importDialog.mrExaminationExamExecutiveCB.getSelectedItem();
			Date examinationDate = (Date) mainWindow.importDialog.mrExaminationDateDP.getModel().getValue();
			String examinationComment = mainWindow.importDialog.mrExaminationCommentTF.getText();
			examinationDTO.setExaminationDate(examinationDate);
			examinationDTO.setComment(examinationComment);

			/**
			 * TODO handle investigators here or decide finally to delete them in sh-ng
			 */
			examinationDTO = shanoirUploaderServiceClient.createExamination(examinationDTO);
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
		 * 3. Fill import-job.json
		 */
		//Exchange exchange = prepareExchange(mainWindow.importDialog, subject.getName(), subject.getId(), examinationId);
		ImportJob importJob = ImportUtils.prepareImportJob(uploadJob, subject.getName(), subject.getId(), examinationId, (Study) mainWindow.importDialog.studyCB.getSelectedItem(), (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem());
		Runnable runnable = new ImportFinishRunnable(uploadJob, uploadFolder, importJob, subject.getName());
		Thread thread = new Thread(runnable);
		thread.start();
		
		mainWindow.importDialog.setVisible(false);
		mainWindow.importDialog.mrExaminationExamExecutiveLabel.setVisible(true);
		mainWindow.importDialog.mrExaminationExamExecutiveCB.setVisible(true);
		mainWindow.setCursor(null); // turn off the wait cursor
		((JButton) event.getSource()).setEnabled(true);
		
		JOptionPane.showMessageDialog(mainWindow.frame,
				ShUpConfig.resourceBundle.getString("shanoir.uploader.import.start.auto.import.message"),
				"Import", JOptionPane.INFORMATION_MESSAGE);
	}
	


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
		subjectDTO.setSubjectStudyList(new ArrayList<SubjectStudy>());
		return subjectDTO;
	}

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

}
