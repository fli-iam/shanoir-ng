package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.PreImportData;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.Center;
import org.shanoir.uploader.model.Investigator;
import org.shanoir.uploader.model.PseudonymusHashValues;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.StudyCard;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.service.rest.dto.ExaminationDTO;
import org.shanoir.uploader.service.rest.dto.HemisphericDominance;
import org.shanoir.uploader.service.rest.dto.IdName;
import org.shanoir.uploader.service.rest.dto.ImagedObjectCategory;
import org.shanoir.uploader.service.rest.dto.Sex;
import org.shanoir.uploader.service.rest.dto.SubjectDTO;
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
	
	private SubjectDTO subjectDTO;
	
	private ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	public ImportFinishActionListenerNG(final MainWindow mainWindow, UploadJob uploadJob, File uploadFolder, SubjectDTO subjectDTO) {
		this.mainWindow = mainWindow;
		this.uploadJob = uploadJob;
		this.uploadFolder = uploadFolder;
		this.subjectDTO = subjectDTO;
		this.shanoirUploaderServiceClientNG = ShUpOnloadConfig.getShanoirUploaderServiceClientNG();
	}

	/**
	 * This method contains all the logic which is performed when the start import
	 * button is clicked.
	 */
	public void actionPerformed(final ActionEvent event) {
		final Study study = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
//		final StudyCard studyCard = (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem();
//		if (study == null || study.getId() == null || studyCard == null || studyCard.getId() == null) {
//			return;
//		}
		
		// block further action
		((JButton) event.getSource()).setEnabled(false);
		mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// TODO the next lines are totally strange as GUI and two different DTOs are used: totally strange,
		// but I did not refactor here as too time demanding at the moment and bad legacy of developer before
		Long subjectId = null;
		String subjectName = null;
		if (subjectDTO == null) {
			SubjectDTO subjectDTO = null;
			try {
				 subjectDTO = fillSubjectDTO(mainWindow.importDialog, uploadJob);
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			}
			subjectDTO = shanoirUploaderServiceClientNG.createSubject(study.getId(), new Long(1), ShUpConfig.isModeSubjectCommonNameManual(), subjectDTO);
			if (subjectDTO == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			} else {
				subjectId = new Long(subjectDTO.getId());
				subjectName = subjectDTO.getName();
				logger.info("Auto-Import: subject created on server with ID: " + subjectId);
			}
		} else {
			subjectId = subjectDTO.getId();
			subjectName = subjectDTO.getName();
			logger.info("Auto-Import: subject used on server with ID: " + subjectId);
		}
		Long examinationId = null;
		if (mainWindow.importDialog.mrExaminationNewExamCB.isSelected()) {
			ExaminationDTO examinationDTO = new ExaminationDTO();
			IdName studyIdName = new IdName(study.getId(), study.getName());
			examinationDTO.setStudy(studyIdName);
			IdName subjectIdName = new IdName(subjectDTO.getId(), subjectDTO.getName());
			examinationDTO.setSubject(subjectIdName);
			Center center = (Center) mainWindow.importDialog.mrExaminationCenterCB.getSelectedItem();
			Investigator investigator = (Investigator) mainWindow.importDialog.mrExaminationExamExecutiveCB.getSelectedItem();
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
			ExaminationDTO examinationDTO = (ExaminationDTO) mainWindow.importDialog.mrExaminationExistingExamCB.getSelectedItem();
			examinationId = examinationDTO.getId();
			logger.info("Auto-Import: examination used on server with ID: " + examinationId);
		}
		
		/**
		 * 3. Fill PreImportData, later added to upload-job.xml
		 */
		PreImportData preImportData = fillPreImportData(mainWindow.importDialog, subjectId, examinationId);
		Runnable runnable = new ImportFinishRunnable(uploadJob, uploadFolder, preImportData, subjectName);
		Thread thread = new Thread(runnable);
		thread.start();
		
		mainWindow.importDialog.setVisible(false);
		mainWindow.setCursor(null); // turn off the wait cursor
		((JButton) event.getSource()).setEnabled(true);
		
		JOptionPane.showMessageDialog(mainWindow.frame,
				ShUpConfig.resourceBundle.getString("shanoir.uploader.import.start.auto.import.message"),
				"Import", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * 
	 * @param importDialog
	 * @param dicomData
	 * @return
	 * @throws ParseException 
	 */
	private SubjectDTO fillSubjectDTO(final ImportDialog importDialog, final UploadJob uploadJob) throws ParseException {
		final SubjectDTO subjectDTO = new SubjectDTO();
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
//		final SubjectStudyDTO subjectStudyDTO = new SubjectStudyDTO();
//		subjectStudyDTO.setSubjectType((SubjectType) importDialog.subjectTypeCB.getSelectedItem());
//		subjectStudyDTO.setPhysicallyInvolved(importDialog.subjectIsPhysicallyInvolvedCB.isSelected());
//		List<SubjectStudyDTO> subjectStudyList = new ArrayList<SubjectStudyDTO>();
//		subjectDTO.setSubjectStudyList(subjectStudyList);
//		subjectDTO.getSubjectStudyList().add(subjectStudyDTO);
		return subjectDTO;
	}

	/**
	 * @param dicomData
	 * @param subjectDTO
	 */
	private void fillPseudonymusHashValues(final UploadJob uploadJob, final SubjectDTO subjectDTO) {
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

	private PreImportData fillPreImportData(ImportDialog importDialog, Long subjectId, Long examinationId) {
		PreImportData preImportData = new PreImportData();
		preImportData.setAutoImportEnable(true);
		
		org.shanoir.dicom.importer.Study exportStudy = new org.shanoir.dicom.importer.Study();
		Study study = (Study) importDialog.studyCB.getSelectedItem();
		exportStudy.setId(study.getId().intValue());
		exportStudy.setName(study.getName());
		preImportData.setStudy(exportStudy);
		
		org.shanoir.dicom.importer.StudyCard exportStudyCard = new org.shanoir.dicom.importer.StudyCard();
		StudyCard studyCard = (StudyCard) importDialog.studyCardCB.getSelectedItem();
		exportStudyCard.setId(studyCard.getId().intValue());
		exportStudyCard.setName(studyCard.getName());
		preImportData.setStudycard(exportStudyCard);
		
		preImportData.setSubjectId(subjectId);
		preImportData.setExaminationId(examinationId);
		return preImportData;
	}

}
