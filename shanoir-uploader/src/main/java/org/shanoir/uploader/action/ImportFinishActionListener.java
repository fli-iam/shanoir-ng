package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.PreImportData;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.Center;
import org.shanoir.uploader.model.Investigator;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.StudyCard;
import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.service.soap.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.Util;
import org.shanoir.ws.generated.uploader.PseudonymusHashValues;
import org.shanoir.ws.generated.uploader.SubjectDTO;
import org.shanoir.ws.generated.uploader.SubjectStudyDTO;

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
	
	private org.shanoir.uploader.model.dto.SubjectDTO subjectDTO;
	
	private ShanoirUploaderServiceClient shanoirUploaderServiceClient;
	
	private ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBIL;

	public ImportFinishActionListener(final MainWindow mainWindow, UploadJob uploadJob, File uploadFolder, org.shanoir.uploader.model.dto.SubjectDTO subjectDTO,
			ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBIL) {
		this.mainWindow = mainWindow;
		this.uploadJob = uploadJob;
		this.uploadFolder = uploadFolder;
		this.subjectDTO = subjectDTO;
		this.importStudyAndStudyCardCBIL = importStudyAndStudyCardCBIL;
		this.shanoirUploaderServiceClient = ShUpOnloadConfig.getShanoirUploaderServiceClient();
	}

	/**
	 * This method contains all the logic which is performed when the start import
	 * button is clicked.
	 */
	public void actionPerformed(final ActionEvent event) {
		final Study study = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
		final StudyCard studyCard = (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem();
		if (study == null || study.getId() == null || studyCard == null || studyCard.getId() == null) {
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

		// TODO the next lines are totally strange as GUI and two different DTOs are used: totally strange,
		// but I did not refactor here as too time demanding at the moment and bad legacy of developer before
		Long subjectId = null;
		String subjectName = null;
		// No existing subject found
		if (subjectDTO == null) {
			SubjectDTO subjectDTOWSDL = null;
			try {
				 subjectDTOWSDL = fillSubjectDTO(mainWindow.importDialog, uploadJob, study.getId());
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			}
			subjectDTOWSDL = shanoirUploaderServiceClient.createSubject(studyCard.getId(), ShUpConfig.isModeSubjectCommonNameManual(), subjectDTOWSDL);
			if (subjectDTOWSDL == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			} else {
				subjectId = new Long(subjectDTOWSDL.getId());
				subjectName = subjectDTOWSDL.getName();
				handleSubjectStudy(study, subjectId);
				logger.info("Auto-Import: subject created on server with ID: " + subjectId);
			}
		// Existing subject found: but check if already in selected study (RelSubjectStudy)
		} else {
			subjectId = subjectDTO.getId();
			subjectName = subjectDTO.getName();
			handleSubjectStudy(study, subjectId);
			logger.info("Auto-Import: subject used on server with ID: " + subjectId);
		}
		Long examinationId = null;
		if (mainWindow.importDialog.mrExaminationNewExamCB.isSelected()) {
			Center center = (Center) mainWindow.importDialog.mrExaminationCenterCB.getSelectedItem();
			Investigator investigator = (Investigator) mainWindow.importDialog.mrExaminationExamExecutiveCB.getSelectedItem();
			Date examinationDate = (Date) mainWindow.importDialog.mrExaminationDateDP.getModel().getValue();
			String examinationComment = mainWindow.importDialog.mrExaminationCommentTF.getText();
			if (investigator != null && center != null && examinationDate != null) {
				long createExaminationId = shanoirUploaderServiceClient.createExamination(study.getId(), subjectId, new Long(center.getId()),
						new Long(investigator.getId()), examinationDate, examinationComment);
				if (createExaminationId == -1) {
					JOptionPane.showMessageDialog(mainWindow.frame,
							mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.createmrexamination"),
							"Error", JOptionPane.ERROR_MESSAGE);
					mainWindow.setCursor(null); // turn off the wait cursor
					((JButton) event.getSource()).setEnabled(true);
					return;
				} else {
					examinationId = new Long(createExaminationId);
					logger.info("Auto-Import: examination created on server with ID: " + examinationId);
				}
			} else {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.createmrexamination"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
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

	private void handleSubjectStudy(final Study study, final Long subjectId) {
		if (importStudyAndStudyCardCBIL.getSubjectStudyDTO() == null) {
			SubjectStudyDTO subjectStudyDTO = new SubjectStudyDTO();
			subjectStudyDTO.setStudyId(study.getId());
			subjectStudyDTO.setSubjectId(subjectId);
			subjectStudyDTO.setSubjectStudyIdentifier(mainWindow.importDialog.subjectStudyIdentifierTF.getText());
			subjectStudyDTO.setSubjectType((String) mainWindow.importDialog.subjectTypeCB.getSelectedItem());
			subjectStudyDTO.setPhysicallyInvolved(mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.isSelected());
			subjectStudyDTO = shanoirUploaderServiceClient.createSubjectStudy(subjectStudyDTO);
			logger.info("Auto-import: RelSubjectStudy created with id: " + subjectStudyDTO.getId() + " for subject: " + subjectId + " in study: " + study.getName());
		}
	}

	/**
	 * This method fills the SubjectDTO and the RelSubjectStudy that will be send to the web service
	 * to create a new subject and integrate it into a study.
	 * 
	 * @param importDialog
	 * @param dicomData
	 * @return
	 * @throws ParseException 
	 */
	private SubjectDTO fillSubjectDTO(final ImportDialog importDialog, final UploadJob uploadJob, final Long studyId) throws ParseException {
		final SubjectDTO subjectDTO = new SubjectDTO();
		if (ShUpConfig.isModeSubjectCommonNameManual()) {
			subjectDTO.setName(importDialog.subjectTextField.getText());
		}
		subjectDTO.setIdentifier(uploadJob.getSubjectIdentifier());
        Date birthDate = ShUpConfig.formatter.parse(uploadJob.getPatientBirthDate());
		XMLGregorianCalendar birthDateXMLGC = Util.toXMLGregorianCalendar(birthDate);
		subjectDTO.setBirthDate(birthDateXMLGC);
		subjectDTO.setSex(uploadJob.getPatientSex());
		subjectDTO.setImagedObjectCategory((String) importDialog.subjectImageObjectCategoryCB.getSelectedItem());
		subjectDTO.setLanguageHemisphericDominance((String) importDialog.subjectLanguageHemisphericDominanceCB.getSelectedItem());
		subjectDTO.setManualHemisphericDominance((String) importDialog.subjectManualHemisphericDominanceCB.getSelectedItem());
		if (ShUpConfig.isModePseudonymus()) {
			fillPseudonymusHashValues(uploadJob, subjectDTO);
		}
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
