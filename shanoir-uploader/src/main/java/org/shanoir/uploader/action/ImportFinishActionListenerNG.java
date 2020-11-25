package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.Serie;
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
import org.shanoir.uploader.model.rest.importer.Instance;
import org.shanoir.uploader.model.rest.importer.Patient;
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
		if (ShUpConfig.isModeSubjectCommonNameManual()) {
			// minimal length for subject common name is 2, same for subject study identifier
			if (mainWindow.importDialog.subjectTextField.getText().length() < 2
				|| (!mainWindow.importDialog.subjectStudyIdentifierTF.getText().isEmpty()
						&& mainWindow.importDialog.subjectStudyIdentifierTF.getText().length() < 2)) {
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
			addSubjectStudy(study, subject);
			// create subject with subject-study filled to avoid access denied exception because of rights check
			subject = shanoirUploaderServiceClientNG.createSubject(subject, ShUpConfig.isModeSubjectCommonNameManual(), studyCard.getCenterId());
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
				addSubjectStudy(study, subject);
				if (shanoirUploaderServiceClientNG.createSubjectStudy(subject) == null) {
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
		 * 3. Fill import-job.json
		 */
		//Exchange exchange = prepareExchange(mainWindow.importDialog, subject.getName(), subject.getId(), examinationId);
		ImportJob importJob = prepareImportJob(mainWindow.importDialog, uploadJob, subject.getName(), subject.getId(), examinationId);
		Runnable runnable = new ImportFinishRunnableNG(uploadJob, uploadFolder, importJob, subject.getName());
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
	
	private ImportJob prepareImportJob(ImportDialog importDialog, UploadJob uploadJob, String subjectName, Long subjectId, Long examinationId) {
		ImportJob importJob = new ImportJob();
		importJob.setFromShanoirUploader(true);
		// handle study and study card, using ImportDialog
		Study studyShanoir = (Study) importDialog.studyCB.getSelectedItem();
		importJob.setStudyId(studyShanoir.getId());
		importJob.setStudyName(studyShanoir.getName());
		StudyCard studyCard = (StudyCard) importDialog.studyCardCB.getSelectedItem();
		// MS Datasets does only return StudyCard DTOs without IDs, as name is unique
		// see: /shanoir-ng-datasets/src/main/java/org/shanoir/ng/studycard/model/StudyCard.java
		importJob.setStudyCardName(studyCard.getName());
		importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		importJob.setConverterId(studyCard.getNiftiConverterId());
		// handle patient and subject
		Patient patient = new Patient();
		patient.setPatientID(uploadJob.getSubjectIdentifier());
		Subject subject = new Subject();
		subject.setId(subjectId);
		subject.setName(subjectName);
		importJob.setSubjectName(subjectName);
		patient.setSubject(subject);
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(patient);
		importJob.setPatients(patients);
		// handle study dicom == examination in Shanoir
		List<org.shanoir.uploader.model.rest.importer.Study> studiesImportJob = new ArrayList<org.shanoir.uploader.model.rest.importer.Study>();
		org.shanoir.uploader.model.rest.importer.Study studyImportJob = new org.shanoir.uploader.model.rest.importer.Study();
		studiesImportJob.add(studyImportJob);
		patient.setStudies(studiesImportJob);
		importJob.setExaminationId(examinationId);
		// handle series for study
		final Collection<Serie> seriesShUp = uploadJob.getSeries();
		final List<org.shanoir.uploader.model.rest.importer.Serie> seriesImportJob = new ArrayList<org.shanoir.uploader.model.rest.importer.Serie>();
		for (org.shanoir.dicom.importer.Serie serieShUp : seriesShUp){
			org.shanoir.uploader.model.rest.importer.Serie serieImportJob = new org.shanoir.uploader.model.rest.importer.Serie();
			serieImportJob.setSelected(true);
			serieImportJob.setSeriesInstanceUID(serieShUp.getId());
			serieImportJob.setSeriesNumber(serieShUp.getSeriesNumber());
			serieImportJob.setModality(serieShUp.getModality());
			serieImportJob.setProtocolName(serieShUp.getProtocol());
			seriesImportJob.add(serieImportJob);
			List<Instance> instancesImportJob = new ArrayList<Instance>();
			for (String filename : serieShUp.getFileNames()){
				Instance instance = new Instance();
				String[] myStringArray = {filename};
				instance.setReferencedFileID(myStringArray);
				instancesImportJob.add(instance);
			}
			serieImportJob.setInstances(instancesImportJob);
			serieImportJob.setImagesNumber(serieShUp.getFileNames().size());
		}
		studyImportJob.setSeries(seriesImportJob);
		return importJob;
	}

	private void addSubjectStudy(final Study study, final Subject subject) {
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(new IdName(study.getId(), study.getName()));
		subjectStudy.setSubject(new IdName(subject.getId(), subject.getName()));
		if (! mainWindow.importDialog.subjectStudyIdentifierTF.getText().isEmpty()) {
			subjectStudy.setSubjectStudyIdentifier(mainWindow.importDialog.subjectStudyIdentifierTF.getText());
		}
		subjectStudy.setSubjectType((SubjectType) mainWindow.importDialog.subjectTypeCB.getSelectedItem());
		subjectStudy.setPhysicallyInvolved(mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.isSelected());
		subject.getSubjectStudyList().add(subjectStudy);
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

//	private Exchange prepareExchange(ImportDialog importDialog, String subjectName, Long subjectId, Long examinationId) {
//		Exchange exchange = new Exchange();
//		// Study
//		Study study = (Study) importDialog.studyCB.getSelectedItem();
//		ExStudy exStudy = new ExStudy();
//		exStudy.setStudyName(study.getName());
//		exStudy.setStudyId(study.getId());
//		exchange.setExStudy(exStudy);
//		// StudyCard
//		StudyCard studyCard = (StudyCard) importDialog.studyCardCB.getSelectedItem();
//		ExStudyCard exStudyCard = new ExStudyCard();
//		exStudyCard.setName(studyCard.getName());
//		ArrayList<ExStudyCard> exStudyCards = new ArrayList<ExStudyCard>();
//		exStudyCards.add(exStudyCard);
//		exStudy.setExStudyCards(exStudyCards);
//		// Subject
//		ExSubject exSubject = new ExSubject();
//		exSubject.setSubjectName(subjectName);
//		exSubject.setSubjectId(subjectId);
//		ArrayList<ExSubject> exSubjects = new ArrayList<ExSubject>();
//		exSubjects.add(exSubject);
//		exStudy.setExSubjects(exSubjects);
//		// Examination
//		ExExamination exExamination = new ExExamination();
//		exExamination.setId(examinationId);
//		ArrayList<ExExamination> exExaminations = new ArrayList<ExExamination>();
//		exExaminations.add(exExamination);
//		exSubject.setExExaminations(exExaminations);
//		return exchange;
//	}

}
