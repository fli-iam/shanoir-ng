package org.shanoir.uploader.action;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.MRI;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.upload.UploadJob;

/**
 * This class implements the logic when the start import button is clicked.
 * 
 * @author mkain
 * 
 */
public class ImportDialogOpener {

	private static Logger logger = Logger.getLogger(ImportDialogOpener.class);

	private MainWindow mainWindow;

	private ImportDialog importDialog;

	private ResourceBundle resourceBundle;

	private ShanoirUploaderServiceClient shanoirUploaderServiceClient;

	public ImportDialogOpener(final MainWindow mainWindow,
			final ShanoirUploaderServiceClient shanoirUploaderServiceClient) {
		this.mainWindow = mainWindow;
		this.resourceBundle = mainWindow.resourceBundle;
		this.shanoirUploaderServiceClient = shanoirUploaderServiceClient;
	}

	public void openImportDialog(UploadJob uploadJob, File uploadFolder) {
		try {
			Date studyDate = ShUpConfig.formatter.parse(uploadJob.getStudyDate());
			// get items on server
			Subject subject = getSubject(uploadJob);
			List<Study> studiesWithStudyCards = getStudiesWithStudyCards(uploadJob);
			List<Examination> examinationDTOs = getExaminations(subject);
			// init components of GUI and listeners
			ImportStudyCardFilterDocumentListener importStudyCardFilterDocumentListener = new ImportStudyCardFilterDocumentListener(this.mainWindow);
			ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBIL = new ImportStudyAndStudyCardCBItemListener(this.mainWindow, subject, examinationDTOs, studyDate, importStudyCardFilterDocumentListener);
			ImportFinishActionListener importFinishAL = new ImportFinishActionListener(this.mainWindow, uploadJob, uploadFolder, subject, importStudyAndStudyCardCBIL);
			importDialog = new ImportDialog(this.mainWindow,
					ShUpConfig.resourceBundle.getString("shanoir.uploader.preImportDialog.title"), true, resourceBundle,
					importStudyAndStudyCardCBIL, importFinishAL, importStudyCardFilterDocumentListener);
			// update import dialog with items from server
			updateImportDialogForSubject(subject); // this has to be done after init of the dialog
			updateImportDialogForNewExamFields(studyDate, uploadJob.getStudyDescription());
			updateImportDialogForStudyAndStudyCard(studiesWithStudyCards);
			updateImportDialogForMRICenter(uploadJob);
			importDialog.mrExaminationExamExecutiveLabel.setVisible(false);
			importDialog.mrExaminationExamExecutiveCB.setVisible(false);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
		importDialog.setVisible(true);
	}
	
	/**
	 * @param uploadJob
	 */
	private void updateImportDialogForMRICenter(final UploadJob uploadJob) {
		MRI mriInformation = uploadJob.getMriInformation();
 		String institutionName = mriInformation.getInstitutionName();
		String institutionAddress = mriInformation.getInstitutionAddress();
		String stationName = mriInformation.getStationName();
		String manufacturer = mriInformation.getManufacturer();
		String manufacturersModelName = mriInformation.getManufacturersModelName();
		String magneticFieldStrength = mriInformation.getMagneticFieldStrength();
		String deviceSerialNumber = mriInformation.getDeviceSerialNumber();
		importDialog.mriCenterText.setText(institutionName);
		importDialog.mriCenterAddressText.setText(institutionAddress);
		importDialog.mriStationNameText.setText(stationName);
		importDialog.mriManufacturerText.setText(manufacturer);
		importDialog.mriManufacturersModelNameText.setText(manufacturersModelName);
		importDialog.mriMagneticFieldStrengthText.setText(magneticFieldStrength);
		importDialog.mriDeviceSerialNumberText.setText(deviceSerialNumber);
	}

	/**
	 * This method calls the backend service and transforms DTO into model objects.
	 * 
	 * @param dicomData
	 * @param equipmentDicom
	 * @throws Exception 
	 */
	private List<Study> getStudiesWithStudyCards(final UploadJob uploadJob) throws Exception {
		List<Study> studies = shanoirUploaderServiceClient.findStudiesNamesAndCenters();
		if (studies != null) {
			logger.info("getStudiesWithStudyCards: " + studies.size() + " studies found.");
			List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClient.findAcquisitionEquipments();
			logger.info("findAcquisitionEquipments: " + acquisitionEquipments.size() + " equipments found.");
			List<StudyCard> studyCards = getAllStudyCards(studies);
			logger.info("getAllStudyCards for studies: " + studyCards.size() + " studycards found.");
			for (Iterator<Study> iterator = studies.iterator(); iterator.hasNext();) {
				Study study = (Study) iterator.next();
				study.setCompatible(new Boolean(false));
				Boolean compatibleStudyCard = false;
				if (studyCards != null) {
					List<StudyCard> studyCardsStudy = new ArrayList<StudyCard>();
					for (Iterator<StudyCard> itStudyCards = studyCards.iterator(); itStudyCards.hasNext();) {
						StudyCard studyCard = (StudyCard) itStudyCards.next();
						// filter all study cards related to the selected study
						if (study.getId().equals(studyCard.getStudyId())) {
							studyCardsStudy.add(studyCard);
							Long acquisitionEquipmentId = studyCard.getAcquisitionEquipmentId();
							for (Iterator<AcquisitionEquipment> acquisitionEquipmentsIt = acquisitionEquipments.iterator(); acquisitionEquipmentsIt.hasNext();) {
								AcquisitionEquipment acquisitionEquipment = (AcquisitionEquipment) acquisitionEquipmentsIt.next();
								// find the correct equipment
								if (acquisitionEquipment.getId().equals(acquisitionEquipmentId)) {
									studyCard.setAcquisitionEquipment(acquisitionEquipment);
									// check if values from server are complete, no sense for comparison if no serial number on server
									if (acquisitionEquipment != null
										&& acquisitionEquipment.getManufacturerModel() != null
										&& acquisitionEquipment.getManufacturerModel().getManufacturer() != null
										&& acquisitionEquipment.getSerialNumber() != null) {
										// check if values are present in DICOM
										String deviceSerialNumber = uploadJob.getMriInformation().getDeviceSerialNumber();
										if (deviceSerialNumber != null && !"".equals(deviceSerialNumber)) {
											if (acquisitionEquipment.getSerialNumber().compareToIgnoreCase(deviceSerialNumber) == 0
												|| deviceSerialNumber.contains(acquisitionEquipment.getSerialNumber())) {
												studyCard.setCompatible(true);
												compatibleStudyCard = true;
											} else {
												studyCard.setCompatible(false);
											}
										// no match with server
										} else {
											studyCard.setCompatible(false); // no match, as no value from DICOM or from server exists
										}
									// set in-compatible in case of missing server values
									} else {
										studyCard.setCompatible(false);
									}								
									break; // correct equipment found, break for-loop acqEquip
								}
							}
						}
					}
					if (compatibleStudyCard) {
						study.setCompatible(true);
					} else {
						study.setCompatible(false);
					}
					study.setStudyCards(studyCardsStudy);
				}
			}
			return studies;
		} else {
			return null;
		}
	}

	private List<StudyCard> getAllStudyCards(List<Study> studies) throws Exception {
		IdList idList = new IdList();
		for (Iterator<Study> iterator = studies.iterator(); iterator.hasNext();) {
			Study study = (Study) iterator.next();
			idList.getIdList().add(study.getId());
		}
		List<StudyCard> studyCards = shanoirUploaderServiceClient.findStudyCardsByStudyIds(idList);
		return studyCards;
	}

	/**
	 * @param studiesWithStudyCards
	 */
	private void updateImportDialogForStudyAndStudyCard(List<Study> studiesWithStudyCards) {
		importDialog.studyCB.removeAllItems();
		importDialog.studyCardCB.removeAllItems();
		if (studiesWithStudyCards != null && !studiesWithStudyCards.isEmpty()) {
			boolean firstCompatibleStudyFound = false;
			for (Study study : studiesWithStudyCards) {
				importDialog.studyCB.addItem(study);
				if (study.getCompatible() != null
					&& study.getCompatible()
					&& !firstCompatibleStudyFound) {
					importDialog.studyCB.setSelectedItem(study);
					firstCompatibleStudyFound = true;
					boolean firstCompatibleStudyCardFound = false;
					for (StudyCard studyCard : study.getStudyCards()) {
						if (studyCard.getCompatible() != null
							&& studyCard.getCompatible()
							&& !firstCompatibleStudyCardFound) {
							importDialog.studyCardCB.setSelectedItem(studyCard);
							firstCompatibleStudyCardFound = true;
						}
					}
				}
			}
			if (!firstCompatibleStudyFound) {
				// this selectItem adds study cards to the stuyCardCB in case of no
				// compatible study found, see ImportStudyAndStudyCardCBItemListener
				Study firstStudy = studiesWithStudyCards.get(0);
				importDialog.studyCB.setSelectedItem(firstStudy);
				if (firstStudy.getStudyCards() != null && !firstStudy.getStudyCards().isEmpty()) {
					StudyCard firstStudyCard = firstStudy.getStudyCards().get(0);
					// this selectItem adds centers to the newly-create-exam
					importDialog.studyCardCB.setSelectedItem(firstStudyCard);					
				}
			}
		}
		importDialog.studyCB.setValueSet(false);
	}

	/**
	 * @param dicomData
	 * @throws Exception
	 */
	private Subject getSubject(final UploadJob uploadJob) throws Exception {
		Subject foundSubject = null;
		if (uploadJob.getSubjectIdentifier() != null) {
			foundSubject = shanoirUploaderServiceClient
					.findSubjectBySubjectIdentifier(uploadJob.getSubjectIdentifier());
		}
		return foundSubject;
	}

	private void updateImportDialogForSubject(Subject subject) {
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
		if (subject != null) {
			// Manage subject values here:
			importDialog.subjectTextField.setText(subject.getName());
			importDialog.subjectTextField.setBackground(Color.LIGHT_GRAY);
			importDialog.subjectTextField.setEnabled(false);
			importDialog.subjectTextField.setEditable(false);
			importDialog.subjectTextField.setValueSet(true);
			importDialog.subjectImageObjectCategoryCB.setSelectedItem(subject.getImagedObjectCategory());
			importDialog.subjectImageObjectCategoryCB.setEnabled(false);
			importDialog.subjectLanguageHemisphericDominanceCB
					.setSelectedItem(subject.getLanguageHemisphericDominance());
			importDialog.subjectLanguageHemisphericDominanceCB.setEnabled(false);
			importDialog.subjectManualHemisphericDominanceCB
					.setSelectedItem(subject.getManualHemisphericDominance());
			importDialog.subjectManualHemisphericDominanceCB.setEnabled(false);
			importDialog.subjectPersonalCommentTextArea.setBackground(Color.LIGHT_GRAY);
			importDialog.subjectPersonalCommentTextArea.setEditable(false);
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
		}
	}

	private List<Examination> getExaminations(Subject subjectDTO) throws Exception {
		if (subjectDTO != null) {
			List<Examination> examinationList = shanoirUploaderServiceClient
					.findExaminationsBySubjectId(subjectDTO.getId());
			return examinationList;
		}
		return null;
	}

	private void updateImportDialogForNewExamFields(Date studyDate, String studyDescription)
			throws ParseException {
		importDialog.mrExaminationNewDateModel.setValue(studyDate);
		importDialog.mrExaminationCommentTF.setText(studyDescription);
	}

}
