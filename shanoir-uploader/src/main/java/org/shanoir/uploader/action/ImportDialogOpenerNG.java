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
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.uploader.ShUpConfig;
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
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;

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
		try {
			Date studyDate = ShUpConfig.formatter.parse(uploadJob.getStudyDate());
			// get items on server
			Subject subject = getSubject(uploadJob);
			List<Study> studiesWithStudyCards = getStudiesWithStudyCards(uploadJob);
			List<Examination> examinationDTOs = getExaminations(subject);
			// init components of GUI and listeners
			ImportStudyAndStudyCardCBItemListenerNG importStudyAndStudyCardCBILNG = new ImportStudyAndStudyCardCBItemListenerNG(this.mainWindow, subject, examinationDTOs, studyDate);
			ImportFinishActionListenerNG importFinishALNG = new ImportFinishActionListenerNG(this.mainWindow, uploadJob, uploadFolder, subject, importStudyAndStudyCardCBILNG);
			importDialog = new ImportDialog(this.mainWindow,
					ShUpConfig.resourceBundle.getString("shanoir.uploader.preImportDialog.title"), true, resourceBundle,
					importStudyAndStudyCardCBILNG, importFinishALNG);
			// update import dialog with items from server
			updateImportDialogForSubject(subject); // this has to be done after init of dialog
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
	 * @throws Exception 
	 */
	private List<Study> getStudiesWithStudyCards(final UploadJob uploadJob) throws Exception {
		Serie firstSerie = uploadJob.getSeries().iterator().next();
		String manufacturer = firstSerie.getMriInformation().getManufacturer();
		String manufacturerModelName = firstSerie.getMriInformation().getManufacturersModelName();
		String deviceSerialNumber = firstSerie.getMriInformation().getDeviceSerialNumber();
		List<Study> studies = shanoirUploaderServiceClientNG.findStudiesNamesAndCenters();
		logger.info("getStudiesWithStudyCards: " + studies.size() + " studies found.");
		if (studies != null) {
			List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClientNG.findAcquisitionEquipments();
			logger.info("findAcquisitionEquipments: " + acquisitionEquipments.size() + " equipments found.");
			List<StudyCard> studyCards = getAllStudyCards(studies);
			logger.info("getAllStudyCards: " + studyCards.size() + " studycards found.");
			for (Iterator<Study> iterator = studies.iterator(); iterator.hasNext();) {
				Study study = (Study) iterator.next();
				study.setCompatible(new Boolean(false));
				Boolean compatibleStudyCard = false;
				if (studyCards != null) {
					List<StudyCard> studyCardsStudy = new ArrayList<StudyCard>();
					for (Iterator<StudyCard> itStudyCards = studyCards.iterator(); itStudyCards.hasNext();) {
						StudyCard studyCard = (StudyCard) itStudyCards.next();
						if (study.getId() == studyCard.getStudyId()) {
							studyCardsStudy.add(studyCard);
							Long acquisitionEquipmentId = studyCard.getAcquisitionEquipmentId();
							for (Iterator<AcquisitionEquipment> acquisitionEquipmentsIt = acquisitionEquipments.iterator(); acquisitionEquipmentsIt.hasNext();) {
								AcquisitionEquipment acquisitionEquipment = (AcquisitionEquipment) acquisitionEquipmentsIt.next();
								if (acquisitionEquipment.getId() == acquisitionEquipmentId) {
									studyCard.setAcquisitionEquipment(acquisitionEquipment);
									if (acquisitionEquipment != null && acquisitionEquipment.getManufacturerModel() != null
											&& acquisitionEquipment.getManufacturerModel().getManufacturer() != null) {
										if (manufacturer != null && manufacturerModelName != null && deviceSerialNumber != null) {
											String manufacturerSC = acquisitionEquipment.getManufacturerModel().getManufacturer().getName();
											String manufacturerModelNameSC = acquisitionEquipment.getManufacturerModel().getName();
											if (manufacturerSC.compareToIgnoreCase(manufacturer) == 0
													&& manufacturerModelNameSC.compareToIgnoreCase(manufacturerModelName) == 0
													&& acquisitionEquipment.getSerialNumber().compareToIgnoreCase(deviceSerialNumber) == 0) {
												studyCard.setCompatible(true);
												compatibleStudyCard = true;
											} else {
												studyCard.setCompatible(false);
											}
										} else {
											studyCard.setCompatible(false);
										}
									} else {
										studyCard.setCompatible(false);
									}								
									break;
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

	private List<StudyCard> getAllStudyCards(List<Study> studies) {
		IdList idList = new IdList();
		for (Iterator<Study> iterator = studies.iterator(); iterator.hasNext();) {
			Study study = (Study) iterator.next();
			idList.getIdList().add(study.getId());
		}
		List<StudyCard> studyCards = shanoirUploaderServiceClientNG.findStudyCardsByStudyIds(idList);
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
				// compatible study found, see ImportStudyAndStudyCardCBItemListenerNG
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
			foundSubject = shanoirUploaderServiceClientNG
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
			List<Examination> examinationList = shanoirUploaderServiceClientNG
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
