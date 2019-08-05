package org.shanoir.uploader.action;

import java.awt.Color;
import java.awt.Container;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdatepicker.impl.JDatePickerImpl;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.ImportDialog;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.model.Center;
import org.shanoir.uploader.model.Investigator;
import org.shanoir.uploader.model.Study;
import org.shanoir.uploader.model.StudyCard;
import org.shanoir.uploader.model.dto.CenterDTO;
import org.shanoir.uploader.model.dto.EquipmentDicom;
import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.model.dto.InvestigatorDTO;
import org.shanoir.uploader.model.dto.StudyCardDTO;
import org.shanoir.uploader.model.dto.StudyDTO;
import org.shanoir.uploader.model.dto.SubjectDTO;
import org.shanoir.uploader.service.IWebService;
import org.shanoir.uploader.service.WebServiceResponse;
import org.shanoir.uploader.utils.Util;

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

	private IWebService webService;

	public ImportDialogOpener(final MainWindow mainWindow, final IWebService webService) {
		this.mainWindow = mainWindow;
		this.importDialog = mainWindow.importDialog;
		this.resourceBundle = mainWindow.resourceBundle;
		this.webService = webService;
	}

	public void openImportDialog() {
		final UploadJob uploadJob = importDialog.getUploadJob();
		// login again, in case session has been expired
		if (webService.testConnection() == 0) {
			updateImportDialogForMRICenter(uploadJob);
			List<Study> studiesWithStudyCards = getStudiesWithStudyCards(uploadJob);
			updateImportDialogForStudyAndStudyCard(studiesWithStudyCards);
			SubjectDTO subjectDTO = getSubject(uploadJob);
			updateImportDialogForSubject(subjectDTO);
			List<ExaminationDTO> examinationDTOs = getExaminations(subjectDTO);
			try {
				updateImportDialogForExaminations(examinationDTOs, uploadJob);
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
				return;
			}
			mainWindow.importDialog.setVisible(true);
		} else {
			return;
		}
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
		EquipmentDicom equipmentDicom = new EquipmentDicom(manufacturer, manufacturerModelName, deviceSerialNumber);
		List<Study> studiesWithStudyCards = new ArrayList<Study>();
		WebServiceResponse<List<StudyDTO>> webServiceResponse = webService.findStudies(equipmentDicom);
		if (webServiceResponse.getStatusCode() == 0) {
			List<StudyDTO> studyDTOList = webServiceResponse.getObj();
			for (StudyDTO studyDTO : studyDTOList) {
				if (studyDTO != null && !studyDTO.getStudyCards().isEmpty()) {
					final Study study = new Study();
					study.setId(studyDTO.getId());
					study.setName(studyDTO.getName());
					// add centers with investigators
					List<Center> centerWithInvestigators = new ArrayList<Center>();
					for (CenterDTO centerDTO : studyDTO.getCenters()) {
						Center center = new Center(centerDTO.getId(), centerDTO.getName());
						List<Investigator> investigators = new ArrayList<Investigator>();
						if (centerDTO.getInvestigators() != null) {
							for (InvestigatorDTO investigatorDTO : centerDTO.getInvestigators()) {
								Investigator investigator = new Investigator(Long.valueOf(investigatorDTO.getId()),
										investigatorDTO.getName());
								investigators.add(investigator);
							}
							center.setInvestigatorList(investigators);
						}
						centerWithInvestigators.add(center);
					}
					study.setCenters(centerWithInvestigators);
					// add study cards
					Boolean compatibleStudyCard = false;
					List<StudyCard> studyCardList = new ArrayList<StudyCard>();
					for (StudyCardDTO studyCardDTO : studyDTO.getStudyCards()) {
						if (studyCardDTO != null) {
							final StudyCard studyCard = new StudyCard();
							String studyCardName = studyCardDTO.getName();
							if (studyCardDTO.getAcqEquipmentManufacturer() != null) {
								studyCardName = studyCardName + " (" + studyCardDTO.getAcqEquipmentManufacturer()
										+ " - " + studyCardDTO.getAcqEquipmentManufacturerModel() + " "
										+ studyCardDTO.getAcqEquipmentSerialNumber() + " - "
										+ studyCardDTO.getCenterName() + ")";
								if (studyCardDTO.getAcqEquipmentManufacturer().compareToIgnoreCase(manufacturer) == 0
										&& studyCardDTO.getAcqEquipmentManufacturerModel().compareToIgnoreCase(manufacturerModelName) == 0
										&& studyCardDTO.getAcqEquipmentSerialNumber().compareToIgnoreCase(deviceSerialNumber) == 0) {
									studyCard.setCompatible(true);
									compatibleStudyCard = true;
								} else {
									studyCard.setCompatible(false);
								}
							} else {
								studyCard.setCompatible(false);
							}
							studyCard.setName(studyCardName);
							studyCard.setId(studyCardDTO.getId());
							studyCard.setCenter(new org.shanoir.uploader.model.Center(studyCardDTO.getCenterId(),
									studyCardDTO.getCenterName()));
							studyCardList.add(studyCard);
						}
					}
					if (compatibleStudyCard) {
						study.setCompatible(true);
					} else {
						study.setCompatible(false);
					}
					study.setStudyCards(studyCardList);
					studiesWithStudyCards.add(study);
				}
			}
		} else {
			logger.error("Unable to retrieve Study/StudyCard list using study webservice : "
					+ webServiceResponse.getStatus());
			JOptionPane.showMessageDialog(mainWindow.frame,
					resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.studyfinder"),
					"WSDL Service error STUDY", JOptionPane.ERROR_MESSAGE);
		}
		return studiesWithStudyCards;
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
				if (study.getCompatible() && !firstCompatibleStudyFound) {
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
	 */
	private SubjectDTO getSubject(final UploadJob uploadJob) {
		SubjectDTO foundSubject = null;
		if (uploadJob.getSubjectIdentifier() != null) {
			WebServiceResponse<SubjectDTO> webServiceResponseForSubject = webService
					.findSubjectByIdentifier(uploadJob.getSubjectIdentifier());
			if (webServiceResponseForSubject != null) {
				switch (webServiceResponseForSubject.getStatusCode()) {
				case 0:
					foundSubject = webServiceResponseForSubject.getObj();
					break;
				case -1:
					logger.error(resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectfinder")
							+ " : " + webServiceResponseForSubject.getStatus());
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectfinder"),
							"WSDL Service error SUBJECT", JOptionPane.ERROR_MESSAGE);
					break;
				case -2:
					logger.error("Unable to find subject using Subject Finder method findSubjectByIdentifier("
							+ uploadJob.getSubjectIdentifier() + ") in wsdl webservice."
							+ webServiceResponseForSubject.getStatus());
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle
									.getString("shanoir.uploader.systemErrorDialog.error.wsdl.subjectfinder.find"),
							"WSDL Service error SUBJECT", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		}
		return foundSubject;
	}

	/**
	 * @param subjectDTO
	 */
	private void updateImportDialogForSubject(SubjectDTO subjectDTO) {
		// Existing subject found with identifier:
		if (subjectDTO != null) {
			importDialog.setSubjectDTO(subjectDTO);
			// Common name
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
			// No existing subject found with identifier:
		} else {
			importDialog.setSubjectDTO(null);
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
			importDialog.subjectImageObjectCategoryCB.setSelectedItem(importDialog.imageObjectCategories[1]);

			importDialog.subjectLanguageHemisphericDominanceCB.setEnabled(true);
			importDialog.subjectLanguageHemisphericDominanceCB.setSelectedItem(importDialog.leftOrRightLanguage[0]);
			importDialog.subjectManualHemisphericDominanceCB.setEnabled(true);
			importDialog.subjectManualHemisphericDominanceCB.setSelectedItem(importDialog.leftOrRightManual[0]);

			importDialog.subjectPersonalCommentTextArea.setText("");
			importDialog.subjectPersonalCommentTextArea.setBackground(Color.WHITE);
			importDialog.subjectPersonalCommentTextArea.setEditable(true);
		}
		importDialog.subjectIsPhysicallyInvolvedCB.setSelected(true);
		importDialog.subjectTypeCB.setSelectedItem(importDialog.subjectTypeValues[1]);
	}

	private List<ExaminationDTO> getExaminations(SubjectDTO subjectDTO) {
		if (subjectDTO != null) {
			WebServiceResponse<List<ExaminationDTO>> webServiceResponseForExaminations = webService
					.findExaminationsBySubjectId(subjectDTO.getId());
			if (webServiceResponseForExaminations != null) {
				switch (webServiceResponseForExaminations.getStatusCode()) {
				case 0:
					List<ExaminationDTO> examinationDTOs = webServiceResponseForExaminations.getObj();
					return examinationDTOs;
				case -1:
				case -2:
					logger.error(resourceBundle
							.getString("shanoir.uploader.systemErrorDialog.error.wsdl.mrexaminationfinder") + " : "
							+ webServiceResponseForExaminations.getStatus());
					JOptionPane.showMessageDialog(mainWindow.frame,
							resourceBundle
									.getString("shanoir.uploader.systemErrorDialog.error.wsdl.mrexaminationfinder"),
							"WSDL Service error EXAM", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		}
		return null;
	}

	private void updateImportDialogForExaminations(List<ExaminationDTO> examinationDTOs, UploadJob uploadJob) throws ParseException {
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
