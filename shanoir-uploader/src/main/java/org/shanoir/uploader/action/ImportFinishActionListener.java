package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.upload.UploadState;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.QualityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the logic when the start import button is clicked.
 * 
 * @author mkain
 * 
 */
public class ImportFinishActionListener implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(ImportFinishActionListener.class);

	private MainWindow mainWindow;
	
	private File uploadFolder;
	
	private Subject subjectREST;
	
	private ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBILNG;

	public ImportFinishActionListener(final MainWindow mainWindow, File uploadFolder, Subject subjectREST,
			ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBILNG) {
		this.mainWindow = mainWindow;
		this.uploadFolder = uploadFolder;
		this.subjectREST = subjectREST;
		this.importStudyAndStudyCardCBILNG = importStudyAndStudyCardCBILNG;
	}

	/**
	 * This method contains all the logic which is performed when the start import
	 * button is clicked.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final Study study = (Study) mainWindow.importDialog.studyCB.getSelectedItem();
		if (study == null || study.getId() == null) {
			JOptionPane.showMessageDialog(mainWindow.frame,
					mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.import.study"),
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ImportJob importJob = null;
		try {
			importJob = ImportUtils.readImportJob(uploadFolder);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(mainWindow.frame,
					mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.import.study"),
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		Long centerId = null;
		AcquisitionEquipment equipment = null;
		if (study.isWithStudyCards()) {
			final StudyCard studyCard = (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem();
			if (studyCard == null || studyCard.getName() == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.import.study"),
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			equipment = studyCard.getAcquisitionEquipment();
			centerId = studyCard.getAcquisitionEquipment().getCenter().getId();
		} else {
			InstitutionDicom institutionDicom = new InstitutionDicom();
			institutionDicom.setInstitutionName(mainWindow.importDialog.mriCenterText.getText());
			institutionDicom.setInstitutionAddress(mainWindow.importDialog.mriCenterAddressText.getText());
			Center center = ImportUtils.findOrCreateCenterWithInstitutionDicom(institutionDicom, study.getId());
			if (center != null) {
				centerId = center.getId();
				EquipmentDicom equipmentDicom = importJob.getFirstSelectedSerie().getEquipment();
				equipmentDicom.setManufacturer(mainWindow.importDialog.mriManufacturerText.getText());
				equipmentDicom.setManufacturerModelName(mainWindow.importDialog.mriManufacturersModelNameText.getText());
				equipmentDicom.setDeviceSerialNumber(mainWindow.importDialog.mriDeviceSerialNumberText.getText());
				equipment = ImportUtils.findOrCreateEquipmentWithEquipmentDicom(equipmentDicom, center);
				if (equipment == null) {
					logger.error("No study card: equipment not found or created.");
					JOptionPane.showMessageDialog(mainWindow.frame,
							"Equipment not found or created.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				logger.error("No study card: center not found or created.");
				JOptionPane.showMessageDialog(mainWindow.frame,
						"Center not found or created.",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		// block further action
		((JButton) event.getSource()).setEnabled(false);
		mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		/**
		 * In case of Neurinfo: the user can either enter a new common name to create a new subject
		 * or select an existing subject from the combo box. This is not possible for OFSEP profile.
		 */
		boolean useExistingSubjectInStudy = false;
		if (ShUpConfig.isModeSubjectNameManual()) {
			// minimal length for subject common name is 1, same for subject study identifier
			// if nothing is entered, use existing subject selected
			if (mainWindow.importDialog.existingSubjectsCB.isEnabled()) {
				subjectREST = (Subject) mainWindow.importDialog.existingSubjectsCB.getSelectedItem();
				if (subjectREST != null) {
					logger.info("Existing subject used from server with ID: " + subjectREST.getId() + ", name: " + subjectREST.getName());
					useExistingSubjectInStudy = true;
				} else {
					JOptionPane.showMessageDialog(mainWindow.frame,
							mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.subject.creation"),
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		}
		
		// In case user selects existing subject from study, just use it
		if (!useExistingSubjectInStudy) {
			// subject name: entered by the user in the GUI
			String subjectName = mainWindow.importDialog.subjectTextField.getText();
			ImagedObjectCategory category = (ImagedObjectCategory) mainWindow.importDialog.subjectImageObjectCategoryCB.getSelectedItem();
			String languageHemDom = (String) mainWindow.importDialog.subjectLanguageHemisphericDominanceCB.getSelectedItem();
			String manualHemDom = (String) mainWindow.importDialog.subjectManualHemisphericDominanceCB.getSelectedItem();
			SubjectStudy subjectStudy = importStudyAndStudyCardCBILNG.getSubjectStudy();
			String subjectStudyIdentifier = mainWindow.importDialog.subjectStudyIdentifierTF.getText();
			SubjectType subjectType = (SubjectType) mainWindow.importDialog.subjectTypeCB.getSelectedItem();
			boolean isPhysicallyInvolved = mainWindow.importDialog.subjectIsPhysicallyInvolvedCB.isSelected();
			subjectREST = ImportUtils.manageSubject(
				subjectREST, importJob.getSubject(), subjectName, category, languageHemDom, manualHemDom,
				subjectStudy, subjectType, useExistingSubjectInStudy, isPhysicallyInvolved, subjectStudyIdentifier,
				study, equipment);
			if(subjectREST == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
					mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectcreator.createSubjectFromShup"),
				"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			}
		}
		
		Long examinationId = null;
		// If the user wants to create a new examination
		if (mainWindow.importDialog.mrExaminationNewExamCB.isSelected()) {
			Date examinationDate = (Date) mainWindow.importDialog.mrExaminationDateDP.getModel().getValue();
			String examinationComment = mainWindow.importDialog.mrExaminationCommentTF.getText();
			examinationId = ImportUtils.createExamination(study, subjectREST, examinationDate, examinationComment, centerId);
			if (examinationId == null) {
				JOptionPane.showMessageDialog(mainWindow.frame,
						mainWindow.resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.createmrexamination"),
						"Error", JOptionPane.ERROR_MESSAGE);
				mainWindow.setCursor(null); // turn off the wait cursor
				((JButton) event.getSource()).setEnabled(true);
				return;
			} else {
				logger.info("Examination created on server with ID: " + examinationId);
			}
		// If the user wants to use an existing examination
		} else {
			Examination examinationDTO = (Examination) mainWindow.importDialog.mrExaminationExistingExamCB.getSelectedItem();
			examinationId = examinationDTO.getId();
			logger.info("Examination used on server with ID: " + examinationId);
		}
				
		/**
		 * 3. Fill importJob, check quality if needed, start pseudo and prepare upload
		 */
		ImportUtils.prepareImportJob(importJob, subjectREST.getName(), subjectREST.getId(), examinationId, 
			(Study) mainWindow.importDialog.studyCB.getSelectedItem(), (StudyCard) mainWindow.importDialog.studyCardCB.getSelectedItem(), equipment);
		
		// Quality Check if the Study selected has Quality Cards to be checked at import
        try {
			QualityCardResult qualityControlResult = QualityUtils.checkQualityAtImport(importJob, mainWindow.isFromPACS);
			// If quality check resulted in errors, show a message and do not start the import
			if (!qualityControlResult.isEmpty() && (qualityControlResult.hasError())) {
				JOptionPane.showMessageDialog(mainWindow.frame,  QualityUtils.getQualityControlreportScrollPane(qualityControlResult), 
				ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.window.title"), JOptionPane.ERROR_MESSAGE);
				// set status FAILED
				ShUpOnloadConfig.getCurrentNominativeDataController().updateNominativeDataPercentage(uploadFolder, UploadState.ERROR.toString());
				logger.error("The upload for the patient {} failed due to quality control errors.", importJob.getSubject().getName());
			} else {
				// If quality control condition is VALID we do not set a quality card result entry but we update the subjectStudy qualityTag
				if (!qualityControlResult.isEmpty() || !qualityControlResult.getUpdatedSubjectStudies().isEmpty()) {
					// If quality control has one warning or failed valid condition fulfilled we inform the user and allow import to continue
					if (qualityControlResult.hasWarning() || qualityControlResult.hasFailedValid()) {
						JOptionPane.showMessageDialog(mainWindow.frame,  QualityUtils.getQualityControlreportScrollPane(qualityControlResult), 
							ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.window.title"), JOptionPane.WARNING_MESSAGE);
					}
					// If Failed Valid No updated subject studies exist in the qualityControlResult
					// For Now if Failed Valid then the quality tag of the subject on server side is not updated with an empty value
					if (!qualityControlResult.hasFailedValid()) {
						//Set qualityTag to the importJob in order to update subjectStudy qualityTag on server side
						importJob.setQualityTag(qualityControlResult.getUpdatedSubjectStudies().get(0).getQualityTag());
					}
				}				
			}
        } catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			JOptionPane.showMessageDialog(mainWindow.frame, 
				ShUpConfig.resourceBundle.getString("shanoir.uploader.import.quality.check.exception.message") + ex.getMessage(), 
				ShUpConfig.resourceBundle.getString("shanoir.uploader.select.error.title"), JOptionPane.ERROR_MESSAGE);
        }

		Runnable runnable = new ImportFinishRunnable(uploadFolder, importJob, subjectREST.getName());
		Thread thread = new Thread(runnable);
		thread.start();

		JOptionPane.showMessageDialog(mainWindow.frame,
			ShUpConfig.resourceBundle.getString("shanoir.uploader.import.start.auto.import.message"),
		"Import", JOptionPane.INFORMATION_MESSAGE);

		mainWindow.importDialog.setVisible(false);
		mainWindow.importDialog.mrExaminationExamExecutiveLabel.setVisible(true);
		mainWindow.importDialog.mrExaminationExamExecutiveCB.setVisible(true);
		mainWindow.setCursor(null); // turn off the wait cursor
		((JButton) event.getSource()).setEnabled(true);	
	}

}
