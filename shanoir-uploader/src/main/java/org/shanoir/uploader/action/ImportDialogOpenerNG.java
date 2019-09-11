package org.shanoir.uploader.action;

import java.awt.Color;
import java.awt.Container;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFormattedTextField;

import org.apache.log4j.Logger;
import org.jdatepicker.impl.JDatePickerImpl;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.StudyCard;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.service.rest.dto.ExaminationDTO;
import org.shanoir.uploader.service.rest.dto.HemisphericDominance;
import org.shanoir.uploader.service.rest.dto.ImagedObjectCategory;
import org.shanoir.uploader.service.rest.dto.SubjectDTO;
import org.shanoir.uploader.service.rest.dto.SubjectStudyDTO;
import org.shanoir.uploader.service.rest.dto.SubjectType;

/**
 * This class implements the logic when the start import button is clicked.
 * 
 * @author mkain
 * 
 */
public class ImportDialogOpenerNG {

	private static Logger logger = Logger.getLogger(ImportDialogOpenerNG.class);

	private MainWindow mainWindow;

	private ImportDialog importDialog;

	private ResourceBundle resourceBundle;

	private ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	public ImportDialogOpenerNG(final MainWindow mainWindow,
			final ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG) {
		this.mainWindow = mainWindow;
		this.resourceBundle = mainWindow.resourceBundle;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
	}

	public void openImportDialog(UploadJob uploadJob, File uploadFolder) {
		// login again, in case session has been expired
//		if (shanoirUploaderServiceClient.login()) {
			try {
				SubjectDTO subjectDTO = getSubject(uploadJob);
				ImportStudyAndStudyCardCBItemListenerNG importStudyAndStudyCardCBIL = new ImportStudyAndStudyCardCBItemListenerNG(this.mainWindow);
				ImportFinishActionListenerNG importFinishAL = new ImportFinishActionListenerNG(this.mainWindow, uploadJob, uploadFolder, subjectDTO);
				importDialog = new ImportDialog(this.mainWindow,
						ShUpConfig.resourceBundle.getString("shanoir.uploader.preImportDialog.title"), true, resourceBundle,
						importStudyAndStudyCardCBIL, importFinishAL);
				updateImportDialogForSubject(subjectDTO); // this has to be done after init of dialog
				List<Study> studiesWithStudyCards = getStudiesWithStudyCards(uploadJob);
				updateImportDialogForStudyAndStudyCard(studiesWithStudyCards);
				List<ExaminationDTO> examinationDTOs = getExaminations(subjectDTO);
				updateImportDialogForExaminations(examinationDTOs, uploadJob);
				updateImportDialogForMRICenter(uploadJob);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return;
			}
			importDialog.setVisible(true);
//		} else {
//			return;
//		}
	}
	
	/**
	 * @param uploadJob
	 */
	private void updateImportDialogForMRICenter(final UploadJob uploadJob) {
		Serie firstSerie = uploadJob.getSeries().iterator().next();
		String institutionName = firstSerie.getMriInformation().getInstitutionName();
		String institutionAddress = firstSerie.getMriInformation().getInstitutionAddress();
		String stationName = firstSerie.getMriInformation().getStationName();
		String manufacturer = firstSerie.getMriInformation().getManufacturer();
		String manufacturersModelName = firstSerie.getMriInformation().getManufacturersModelName();
		String deviceSerialNumber = firstSerie.getMriInformation().getDeviceSerialNumber();
		importDialog.mriCenterText.setText(institutionName);
		importDialog.mriCenterAddressText.setText(institutionAddress);
		importDialog.mriStationNameText.setText(stationName);
		importDialog.mriManufacturerText.setText(manufacturer);
		importDialog.mriManufacturersModelNameText.setText(manufacturersModelName);
		importDialog.mriDeviceSerialNumberText.setText(deviceSerialNumber);
	}

	/**
	 * This method calls the backend service and transforms DTO into model objects.
	 * Maybe this step could be unified.
	 * 
	 * @param dicomData
	 * @param equipmentDicom
	 */
	private List<Study> getStudiesWithStudyCards(final UploadJob uploadJob) {
		Serie firstSerie = uploadJob.getSeries().iterator().next();
		String manufacturer = firstSerie.getMriInformation().getManufacturer();
		String manufacturerModelName = firstSerie.getMriInformation().getManufacturersModelName();
		String deviceSerialNumber = firstSerie.getMriInformation().getDeviceSerialNumber();
		List<Study> studies = shanoirUploaderServiceClientNG.findStudiesWithStudyCards();
		return studies;
	}

	/**
	 * @param studiesWithStudyCards
	 */
	private void updateImportDialogForStudyAndStudyCard(List<Study> studiesWithStudyCards) {
		if (!studiesWithStudyCards.isEmpty()) {
			importDialog.studyCB.removeAllItems();
			boolean firstCompatibleStudyFound = false;
			for (Study study : studiesWithStudyCards) {
				importDialog.studyCB.addItem(study);
				if (study.getCompatible() != null && !firstCompatibleStudyFound) {
					importDialog.studyCB.setSelectedItem(study);
					importDialog.studyCardCB.removeAllItems();
					boolean firstCompatibleStudyCardFound = false;
					for (StudyCard studyCard : study.getStudyCards()) {
						importDialog.studyCardCB.addItem(studyCard);
						if (studyCard.getCompatible() && !firstCompatibleStudyCardFound) {
							importDialog.studyCardCB.setSelectedItem(studyCard);
							firstCompatibleStudyCardFound = true;
						}
					}
					firstCompatibleStudyFound = true;
				}
			}
		} else {
			importDialog.studyCB.removeAllItems();
			importDialog.studyCardCB.removeAllItems();
		}
		importDialog.studyCB.setValueSet(false);
	}

	/**
	 * @param dicomData
	 * @throws Exception
	 */
	private SubjectDTO getSubject(final UploadJob uploadJob) throws Exception {
		SubjectDTO foundSubject = null;
		if (uploadJob.getSubjectIdentifier() != null) {
			foundSubject = shanoirUploaderServiceClientNG
					.findSubjectBySubjectIdentifier(uploadJob.getSubjectIdentifier());
		}
		return foundSubject;
	}

	private void updateImportDialogForSubject(SubjectDTO subjectDTO) {
		/**
		 * Insert subject specific items into combo boxes from model classes.
		 * Should be there nevertheless if subject exists or not.
		 */
		// Insert ImageObjectCategory objects
		for (int i = 0; i < ImagedObjectCategory.values().length; i++) {
			importDialog.subjectImageObjectCategoryCB.addItem(ImagedObjectCategory.values()[i]);					
		}
		// Insert String here, as the model does not contain "", the unknown, not selected hemdom
		importDialog.subjectLanguageHemisphericDominanceCB.addItem("");
		importDialog.subjectLanguageHemisphericDominanceCB.addItem(HemisphericDominance.Left.getName());
		importDialog.subjectLanguageHemisphericDominanceCB.addItem(HemisphericDominance.Right.getName());
		importDialog.subjectManualHemisphericDominanceCB.addItem("");
		importDialog.subjectManualHemisphericDominanceCB.addItem(HemisphericDominance.Left.getName());
		importDialog.subjectManualHemisphericDominanceCB.addItem(HemisphericDominance.Right.getName());
		for (int i = 0; i < SubjectType.values().length; i++) {
			importDialog.subjectTypeCB.addItem(SubjectType.values()[i]);
		}
		// Existing subject found with identifier:
		if (subjectDTO != null) {
			// Manage subject values here:
			importDialog.subjectTextField.setText(subjectDTO.getName());
			importDialog.subjectTextField.setBackground(Color.LIGHT_GRAY);
			importDialog.subjectTextField.setEnabled(false);
			importDialog.subjectTextField.setEditable(false);
			importDialog.subjectTextField.setValueSet(true);
			importDialog.subjectImageObjectCategoryCB.setSelectedItem(subjectDTO.getImagedObjectCategory());
			importDialog.subjectImageObjectCategoryCB.setEnabled(false);
			importDialog.subjectLanguageHemisphericDominanceCB
					.setSelectedItem(subjectDTO.getLanguageHemisphericDominance());
			importDialog.subjectLanguageHemisphericDominanceCB.setEnabled(false);
			importDialog.subjectManualHemisphericDominanceCB
					.setSelectedItem(subjectDTO.getManualHemisphericDominance());
			importDialog.subjectManualHemisphericDominanceCB.setEnabled(false);
			importDialog.subjectPersonalCommentTextArea.setBackground(Color.LIGHT_GRAY);
			importDialog.subjectPersonalCommentTextArea.setEditable(false);
			// Manage subject_study values here:
			List<SubjectStudyDTO> subjectStudyList = subjectDTO.getSubjectStudyList();
			for (Iterator iterator = subjectStudyList.iterator(); iterator.hasNext();) {
				SubjectStudyDTO subjectStudyDTO = (SubjectStudyDTO) iterator.next();
				importDialog.subjectIsPhysicallyInvolvedCB.setSelected(subjectStudyDTO.isPhysicallyInvolved());
				importDialog.subjectIsPhysicallyInvolvedCB.setEnabled(false);
				importDialog.subjectTypeCB.setSelectedItem(subjectStudyDTO.getSubjectType());
				importDialog.subjectTypeCB.setEnabled(false);
				break; // use the first relation here to display some info
				/**
				 * At this time we have found a subject on using the identifier.
				 * This subject could be in multiple studies, or not, even in a
				 * study not available to the importing user.
				 * The subject could be in the future study, the user chooses to
				 * import into, but could also be in another study. So we display
				 * the first info we have here, as in the current implementation
				 * the user can not change and modify anything on using ShUp.
				 * When we import for the subject and the subject is not yet in
				 * the selected study, we add it automatically to this study on
				 * using the same values as in the other study. This could be
				 * extended later.
				 */
			}
		// No existing subject found with identifier:
		} else {
			// Common name
			if (ShUpConfig.isModeSubjectCommonNameManual()) {
				importDialog.subjectTextField.setText("");
				importDialog.subjectTextField.setBackground(Color.WHITE);
				importDialog.subjectTextField.setEnabled(true);
				importDialog.subjectTextField.setEditable(true);
			} else if (ShUpConfig.isModeSubjectCommonNameAutoIncrement()) {
				importDialog.subjectTextField
						.setText(resourceBundle.getString("shanoir.uploader.import.subject.autofill"));
				importDialog.subjectTextField.setBackground(Color.LIGHT_GRAY);
				importDialog.subjectTextField.setEnabled(false);
				importDialog.subjectTextField.setEditable(false);
			}
			importDialog.subjectTextField.setValueSet(false);
			importDialog.subjectImageObjectCategoryCB.setEnabled(true);
			importDialog.subjectImageObjectCategoryCB.setSelectedItem(ImagedObjectCategory.LIVING_HUMAN_BEING);			
			importDialog.subjectLanguageHemisphericDominanceCB.setEnabled(true);
			importDialog.subjectLanguageHemisphericDominanceCB.setSelectedItem("");			
			importDialog.subjectManualHemisphericDominanceCB.setEnabled(true);
			importDialog.subjectManualHemisphericDominanceCB.setSelectedItem("");
			importDialog.subjectPersonalCommentTextArea.setText("");
			importDialog.subjectPersonalCommentTextArea.setBackground(Color.WHITE);
			importDialog.subjectPersonalCommentTextArea.setEditable(true);
			importDialog.subjectIsPhysicallyInvolvedCB.setEnabled(true);
			importDialog.subjectIsPhysicallyInvolvedCB.setSelected(true);
		}
	}

	private List<ExaminationDTO> getExaminations(SubjectDTO subjectDTO) throws Exception {
		if (subjectDTO != null) {
			List<ExaminationDTO> examinationList = shanoirUploaderServiceClientNG
					.findExaminationsBySubjectId(subjectDTO.getId());
			return examinationList;
		}
		return null;
	}

	private void updateImportDialogForExaminations(List<ExaminationDTO> examinationDTOs, UploadJob uploadJob)
			throws ParseException {
		importDialog.mrExaminationExistingExamCB.removeAllItems();
		if (examinationDTOs != null && !examinationDTOs.isEmpty()) {
			for (Iterator iterator = examinationDTOs.iterator(); iterator.hasNext();) {
				ExaminationDTO examinationDTO = (ExaminationDTO) iterator.next();
				importDialog.mrExaminationExistingExamCB.addItem(examinationDTO);
			}
			importDialog.mrExaminationExistingExamCB.setEnabled(true);
			importDialog.mrExaminationNewExamCB.setEnabled(true);
			importDialog.mrExaminationNewExamCB.setSelected(false);
			disableExaminationNew();
		} else {
			if (importDialog.studyCardCB.getItemCount() > 0) {
				importDialog.mrExaminationNewExamCB.setEnabled(true);
				importDialog.mrExaminationNewExamCB.setSelected(true);
			}
		}
		Date studyDate = ShUpConfig.formatter.parse(uploadJob.getStudyDate());
		importDialog.mrExaminationNewDateModel.setValue(studyDate);
		importDialog.mrExaminationCommentTF.setText(uploadJob.getStudyDescription());
	}

	private void disableExaminationNew() {
		importDialog.mrExaminationExamExecutiveCB.setEnabled(false);
		importDialog.mrExaminationCenterCB.setEnabled(false);
		((Container) importDialog.mrExaminationDateDP).getComponent(1).setEnabled(false);
		JFormattedTextField mrExaminationDateDPTF = ((JDatePickerImpl) importDialog.mrExaminationDateDP)
				.getJFormattedTextField();
		mrExaminationDateDPTF.setBackground(Color.LIGHT_GRAY);
		importDialog.mrExaminationCommentTF.setEnabled(false);
	}

}
