package org.shanoir.uploader.action;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the logic when the start import button is clicked.
 * 
 * @author mkain
 * 
 */
public class ImportDialogOpener {

	private static final Logger logger = LoggerFactory.getLogger(ImportDialogOpener.class);

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

	public void openImportDialog(ImportJob importJob, File importFolder) {
		try {
			Date studyDate = ShUpConfig.formatter.parse(Util.convertLocalDateToString(importJob.getStudy().getStudyDate()));
			Subject subject = null;
			// Profile OFSEP: search with identifier
			if (ShUpConfig.isModeSubjectCommonNameAutoIncrement()) {
				subject = getSubject(importJob);
			} // else Profile Neurinfo: no search with identifier, user selects existing subject 
			List<Study> studiesWithStudyCards = getStudiesWithStudyCards(importJob);
			// init components of GUI and listeners
			ImportStudyCardFilterDocumentListener importStudyCardFilterDocumentListener = new ImportStudyCardFilterDocumentListener(this.mainWindow);
			ImportStudyAndStudyCardCBItemListener importStudyAndStudyCardCBIL = new ImportStudyAndStudyCardCBItemListener(this.mainWindow, subject, studyDate, importStudyCardFilterDocumentListener, shanoirUploaderServiceClient);
			ImportFinishActionListener importFinishAL = new ImportFinishActionListener(this.mainWindow, importJob, importFolder, subject, importStudyAndStudyCardCBIL);
			importDialog = new ImportDialog(this.mainWindow,
					ShUpConfig.resourceBundle.getString("shanoir.uploader.preImportDialog.title"), true, resourceBundle,
					importStudyAndStudyCardCBIL, importFinishAL, importStudyCardFilterDocumentListener);
			// update import dialog with items from server
			updateImportDialogForSubject(subject); // this has to be done after init of the dialog
			updateImportDialogForNewExamFields(studyDate, importJob.getStudy().getStudyDescription());
			updateImportDialogForStudyAndStudyCard(studiesWithStudyCards);
			updateImportDialogForMRICenter(importJob);
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
	private void updateImportDialogForMRICenter(final ImportJob importJob) {
		Serie firstSerie = importJob.getFirstSelectedSerie();
 		String institutionName = firstSerie.getInstitution().getInstitutionName();
		String institutionAddress = firstSerie.getInstitution().getInstitutionAddress();
		String stationName = firstSerie.getEquipment().getStationName();
		String manufacturer = firstSerie.getEquipment().getManufacturer();
		String manufacturersModelName = firstSerie.getEquipment().getManufacturerModelName();
		String magneticFieldStrength = firstSerie.getEquipment().getMagneticFieldStrength();
		String deviceSerialNumber = firstSerie.getEquipment().getDeviceSerialNumber();
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
	private List<Study> getStudiesWithStudyCards(final ImportJob importJob) throws Exception {
		List<Study> studies = shanoirUploaderServiceClient.findStudiesNamesAndCenters();
		if (studies != null) {
			logger.info("getStudiesWithStudyCards: " + studies.size() + " studies found.");
			List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClient.findAcquisitionEquipments();
			logger.info("findAcquisitionEquipments: " + acquisitionEquipments.size() + " equipments found.");
			List<StudyCard> studyCards = ImportUtils.getAllStudyCards(studies);
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
							for (AcquisitionEquipment acquisitionEquipment : acquisitionEquipments) {
								// find the correct equipment for each study card and add it
								if (acquisitionEquipment.getId().equals(studyCard.getAcquisitionEquipmentId())) {
									studyCard.setAcquisitionEquipment(acquisitionEquipment);
								}
							}
							compatibleStudyCard = ImportUtils.flagStudyCardCompatible(
								studyCard, importJob.getFirstSelectedSerie().getEquipment().getManufacturerModelName(),
								importJob.getFirstSelectedSerie().getEquipment().getDeviceSerialNumber());
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

	/**
	 * @param studiesWithStudyCards
	 */
	private void updateImportDialogForStudyAndStudyCard(List<Study> studiesWithStudyCards) {
		importDialog.studyCB.removeAllItems();
		importDialog.studyCardCB.removeAllItems();
		if (studiesWithStudyCards != null && !studiesWithStudyCards.isEmpty()) {
			boolean firstCompatibleStudyFound = false;
			studiesWithStudyCards.sort(Comparator.comparing(Study::getName));
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

	private Subject getSubject(final ImportJob importJob) throws Exception {
		String identifier = importJob.getSubject().getIdentifier();
		if (identifier != null) {
			return shanoirUploaderServiceClient
				.findSubjectBySubjectIdentifier(identifier);
		}
		return null;
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
		// Existing subject found with identifier: only profile OFSEP, not Neurinfo
		if (subject != null) {
			importDialog.subjectTextField.setText(subject.getName());
			importDialog.subjectTextField.setBackground(Color.LIGHT_GRAY);
			importDialog.subjectTextField.setEnabled(false);
			importDialog.subjectTextField.setEditable(false);
			importDialog.subjectTextField.setValueSet(true);
			importDialog.existingSubjectsCB.setVisible(false);
			importDialog.existingSubjectsCB.setBackground(Color.LIGHT_GRAY);
			importDialog.existingSubjectsCB.setEnabled(false);
			importDialog.existingSubjectsCB.setEditable(false);
			ImportStudyAndStudyCardCBItemListener.updateImportDialogForExistingSubject(subject, importDialog);
		// No existing subject found with identifier:
		} else {
			// Profile Neurinfo: enable manual edition
			if (ShUpConfig.isModeSubjectCommonNameManual()) {
				importDialog.subjectTextField.setText("");
				importDialog.subjectTextField.setBackground(Color.WHITE);
				importDialog.subjectTextField.setEnabled(true);
				importDialog.subjectTextField.setEditable(true);
				importDialog.existingSubjectsCB.setEditable(true);
			// Profile OFSEP: display, that subject will be created automatically
			} else if (ShUpConfig.isModeSubjectCommonNameAutoIncrement()) {
				importDialog.subjectTextField
						.setText(resourceBundle.getString("shanoir.uploader.import.subject.autofill"));
				importDialog.subjectTextField.setBackground(Color.LIGHT_GRAY);
				importDialog.subjectTextField.setEnabled(false);
				importDialog.subjectTextField.setEditable(false);
				importDialog.existingSubjectsCB.setVisible(false);
				importDialog.existingSubjectsCB.setBackground(Color.LIGHT_GRAY);
				importDialog.existingSubjectsCB.setEnabled(false);
				importDialog.existingSubjectsCB.setEditable(false);
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

	private void updateImportDialogForNewExamFields(Date studyDate, String studyDescription)
			throws ParseException {
		importDialog.mrExaminationNewDateModel.setValue(studyDate);
		importDialog.mrExaminationCommentTF.setText(studyDescription);
	}

}
