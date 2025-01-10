package org.shanoir.uploader.action;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.PatientVerification;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.shared.dataset.DatasetModalityType;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Manufacturer;
import org.shanoir.uploader.model.rest.ManufacturerModel;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadJobManager;
import org.shanoir.uploader.upload.UploadState;
import org.shanoir.uploader.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportFromTableRunner extends SwingWorker<Void, Integer> {

	private static final Logger logger = LoggerFactory.getLogger(ImportFromTableRunner.class);

	private static final String WILDCARD = "*";
	private static final String WILDCARD_REPLACE = "\\*";

	private Map<String, ImportJob> importJobs;
	private ResourceBundle resourceBundle;
	private ImportFromTableWindow importFromTableWindow;

	private IDicomServerClient dicomServerClient;
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;
	private ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;
	private DownloadOrCopyActionListener dOCAL;

	public ImportFromTableRunner(Map<String, ImportJob> importJobs, ResourceBundle ressourceBundle, ImportFromTableWindow importFromTableWindow, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG, DownloadOrCopyActionListener dOCAL) {
		this.importJobs = importJobs;
		this.resourceBundle = ressourceBundle;
		this.importFromTableWindow = importFromTableWindow;
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
		this.dOCAL = dOCAL;
	}

	@Override
	protected Void doInBackground() throws Exception {
		importFromTableWindow.openButton.setEnabled(false);
		importFromTableWindow.uploadButton.setEnabled(false);

		importFromTableWindow.progressBar.setStringPainted(true);
		importFromTableWindow.progressBar.setString("Preparing import...");
		importFromTableWindow.progressBar.setVisible(true);

		logger.info("Preparing import: loading acquisition equipments and add them to study cards");
		org.shanoir.uploader.model.rest.Study study = (org.shanoir.uploader.model.rest.Study) importFromTableWindow.studyCB.getSelectedItem();
		List<StudyCard> studyCards = study.getStudyCards();
		// as we auto-create new study cards in the process, we can start with an empty list in the study
		if (studyCards == null) {
			studyCards = new ArrayList<StudyCard>();
		}
		// Important use all equipments from database here, as they can be used in N studies
		List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClientNG.findAcquisitionEquipments();
		if (acquisitionEquipments == null) {
			// as we create equipments, we can start with an empty list as well
			acquisitionEquipments = new ArrayList<AcquisitionEquipment>();
		}
		for (AcquisitionEquipment acquisitionEquipment : acquisitionEquipments) {
			for (StudyCard studyCard : studyCards) {
				// find the correct equipment for each study card and add it
				if (acquisitionEquipment.getId().equals(studyCard.getAcquisitionEquipmentId())) {
					studyCard.setAcquisitionEquipment(acquisitionEquipment);
				}
			}
		}

		boolean resultAllJobs = true;
		int i = 1;
		logger.info("\r\n**********************************\r\n"
			+ "Starting Excel mass import...\r\n"
			+ "**********************************");
		for (ImportJob importJob : importJobs.values()) {
			importFromTableWindow.progressBar.setString("Preparing import " + i + "/" + this.importJobs.size());
			importFromTableWindow.progressBar.setValue(100*i/this.importJobs.size() + 1);
			try {
				String patientName = importJob.getDicomQuery().getPatientName();
				String patientID = importJob.getDicomQuery().getPatientID();
				String studyDate = importJob.getDicomQuery().getStudyDate();
				String importJobIdentifier = "[Line: " + i + ", patientName: " + patientName + ", patientID: " + patientID + ", studyDate: " + studyDate + "]";
				logger.info("\r\n------------------------------------------------------\r\n"
					+ "Starting importJob " + importJobIdentifier + "\r\n"
					+ "------------------------------------------------------");
				boolean resultOneJob = importData(importJob, study, acquisitionEquipments);
				resultAllJobs = resultOneJob && resultAllJobs;
				logger.info("\r\n------------------------------------------------------\r\n"
					+ "Finished importJob " + importJobIdentifier + ", success?: " + resultOneJob + "\r\n"
					+ "------------------------------------------------------");
			} catch(Exception exception) {
				logger.error(exception.getMessage(), exception);
			}
			i++;
		}

		if (resultAllJobs) {
			importFromTableWindow.progressBar.setString("Success !");
			importFromTableWindow.progressBar.setValue(100);
			// Open current import tab and close table import panel
			((JTabbedPane) this.importFromTableWindow.scrollPaneUpload.getParent().getParent()).setSelectedComponent(this.importFromTableWindow.scrollPaneUpload.getParent());
			this.importFromTableWindow.frame.setVisible(false);
			this.importFromTableWindow.frame.dispose();
		} else {
			importFromTableWindow.openButton.setEnabled(true);
			importFromTableWindow.uploadButton.setEnabled(false);
		}
		logger.info("\r\n**********************************\r\n"
			+ "Finished Excel mass import...\r\n"
			+ "**********************************");
		return null;
	}

	private boolean importData(ImportJob importJob, org.shanoir.uploader.model.rest.Study studyREST, List<AcquisitionEquipment> acquisitionEquipments) throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException {
		if (!queryPacs(importJob)) {
			return false;
		}
		LocalDate minDate = determineMinDate(importJob);
		if (!selectPatientStudyAndSeries(importJob, minDate)) {
			return false;
		}
		logger.info("DICOM Patient selected: " + importJob.getPatient().toString());
		logger.info("DICOM Study selected: " + importJob.getStudy().toString());
		PatientVerification patientVerification = importJob.getPatientVerification();
		Patient newPatient = ImportUtils.adjustPatientWithPatientVerification(
			importJob.getPatient(),
			patientVerification.getFirstName(),
			patientVerification.getLastName(),
			patientVerification.getBirthName(),
			patientVerification.getBirthDate());
		importJob.setPatient(newPatient);
		Subject subject = dOCAL.createSubjectFromPatient(newPatient);
		importJob.setSubject(subject);

		logger.info("3. Download from PACS");
		/**
		 * For the moment the ImportFromTableRunner processes line-by-line, study-by-study,
		 * so we only send one import job to the DownloadOrCopyRunnable, to download only
		 * one DICOM study, as the code after directly finishes the import of this study.
		 */
		// DownloadOrCopyRunnable sets patient and study to NULL: reduce size of import-job.json
		LocalDate studyDate = importJob.getStudy().getStudyDate();
		String studyDescription = importJob.getStudy().getStudyDescription();
		HashMap<String, ImportJob> downloadImportJobs = new HashMap<String, ImportJob>();
		downloadImportJobs.put(importJob.getStudy().getStudyInstanceUID(), importJob);
		Runnable downloadOrCopyRunnable = new DownloadOrCopyRunnable(true, true, importFromTableWindow.frame, importFromTableWindow.downloadProgressBar,  dicomServerClient, dicomFileAnalyzer,  null, downloadImportJobs);
		Thread downloadThread = new Thread(downloadOrCopyRunnable);
		downloadThread.start();
		while (downloadThread.isAlive()) {
			// wait for download thread to finish 
		}
		File uploadJobFile = new File(importJob.getWorkFolder() + File.separator + UploadJobManager.UPLOAD_JOB_XML);
		UploadJobManager uploadJobManager = new UploadJobManager(uploadJobFile);
		UploadJob uploadJob = uploadJobManager.readUploadJob();

		logger.info("4. Find matching study card in selected study or create a new study card");
		StudyCard studyCard = null;
		List<StudyCard> studyCards = studyREST.getStudyCards();
		if (!studyCards.isEmpty()) {
			// 4.1 Check if study card configured in Excel: use it (user knows best), no DICOM info necessary
			if (importJob.getStudyCardName() != null && !importJob.getStudyCardName().isEmpty()) {
				Optional<StudyCard> scOpt = studyCards.stream().filter(
					element -> element.getName().equals(importJob.getStudyCardName())).findFirst();
				if (scOpt.isPresent()) {
					studyCard = scOpt.get();
					logger.info("Matching study card found in study by name from table: " + studyCard.getName());
				} else {
					uploadJob.setUploadState(UploadState.ERROR);
					importJob.setErrorMessage("Error: study card configured in table, but not found in study: " + importJob.getStudyCardName());
					logger.error(importJob.getErrorMessage());
					return false;
				}
			}
		}
		// 4.2 Find matching study card on study using info from DICOM
		String manufacturerName = uploadJob.getMriInformation().getManufacturer();
		String manufacturerModelName = uploadJob.getMriInformation().getManufacturersModelName();
		String deviceSerialNumber = uploadJob.getMriInformation().getDeviceSerialNumber();
		if (manufacturerName == null || manufacturerName.isBlank()
			|| manufacturerModelName == null || manufacturerModelName.isBlank()
			|| deviceSerialNumber == null || deviceSerialNumber.isBlank()) {
			uploadJob.setUploadState(UploadState.ERROR);
			importJob.setErrorMessage("Error: no manufacturer or model name or device serial number in DICOM.");
			return false;
		}
		logger.info("Manufacturer name used from DICOM: " + manufacturerName);
		logger.info("Manufacturer model name used from DICOM: " + manufacturerModelName);
		logger.info("Device serial number used from DICOM: " + deviceSerialNumber);
		if (!studyCards.isEmpty()) {
			try {
				for (StudyCard studyCardIt : studyCards) {
					if (ImportUtils.flagStudyCardCompatible(studyCardIt, manufacturerModelName, deviceSerialNumber)) {
						studyCard = studyCardIt;
						logger.info("Matching study card found in study: " + studyCard.getName()
							+ " via manufacturer model name: " + manufacturerModelName
							+ " and device serial number: " + deviceSerialNumber);
						break;
					}
				}
			} catch (Exception e) {
				this.importFromTableWindow.error.setText(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
				return false;
			}
		}
		// 4.3 No study card found: create one
		AcquisitionEquipment equipment = null;
		if (studyCard == null) {
			// 4.3.1 try to find equipment via model name and serial number and use it for study card creation
			equipment = ImportUtils.findEquipmentInAllEquipments(acquisitionEquipments, manufacturerModelName, deviceSerialNumber);
			if (equipment != null) {
				// No need to create center, as already existing behind equipment
				studyCard = ImportUtils.createStudyCard(studyREST, equipment, importJob);
				studyCards.add(studyCard); // add in memory to avoid loading from server
			// No equipment found: create one
			} else {				
				String institutionName = uploadJob.getMriInformation().getInstitutionName();
				if (institutionName == null || institutionName.isBlank()) {
					uploadJob.setUploadState(UploadState.ERROR);
					importJob.setErrorMessage("Error: no institution name in DICOM.");
					logger.error(importJob.getErrorMessage());
					return false;
				}
				// 4.3.2 find center or create one, and add it into study for import (study-center)
				InstitutionDicom institutionDicom = new InstitutionDicom();
				institutionDicom.setInstitutionName(institutionName);
				institutionDicom.setInstitutionAddress(uploadJob.getMriInformation().getInstitutionAddress());
				Center center = shanoirUploaderServiceClientNG.findCenterOrCreateByInstitutionDicom(institutionDicom, studyREST.getId());
				if (center == null) {
					uploadJob.setUploadState(UploadState.ERROR);
					importJob.setErrorMessage("Error: could not find or create center.");
					logger.error(importJob.getErrorMessage());
					return false;
				}
				// 4.3.3 find or create manufacturer model and manufacturer
				ManufacturerModel manufacturerModel = ImportUtils.findManufacturerModelInAllEquipments(acquisitionEquipments, manufacturerName, manufacturerModelName);
				if (manufacturerModel == null) { // create one
					Manufacturer manufacturer = ImportUtils.findManufacturerInAllEquipments(acquisitionEquipments, manufacturerName);
					if (manufacturer == null) { // find matching manufacturer or create new manufacturer
						List<Manufacturer> manufacturers = shanoirUploaderServiceClientNG.findManufacturers();
						Optional<Manufacturer> matchingManufacturer = manufacturers.stream()
        					.filter(m -> m.getName().equals(manufacturerName)).findFirst();
						if (matchingManufacturer.isPresent()) {
							manufacturer = matchingManufacturer.get();
						} else {
							manufacturer = ImportUtils.createManufacturer(manufacturerName);
						}
					}
					if (manufacturer == null) {
						uploadJob.setUploadState(UploadState.ERROR);
						importJob.setErrorMessage("Error: could not create manufacturer.");
						logger.error(importJob.getErrorMessage());
						return false;
					}
					String modality = importJob.getDicomQuery().getModality();
					Integer datasetModalityType = DatasetModalityType.getIdFromModalityName(modality);
					String magneticFieldStrength = uploadJob.getMriInformation().getMagneticFieldStrength();
					if (magneticFieldStrength == null || magneticFieldStrength.isBlank() || "unknown".equals(magneticFieldStrength)) {
						magneticFieldStrength = "0.0";
					}
					manufacturerModel = ImportUtils.createManufacturerModel(
						manufacturerModelName, manufacturer, DatasetModalityType.getType(datasetModalityType).toString(), Double.valueOf(magneticFieldStrength));
				}
				if (manufacturerModel == null) {
					uploadJob.setUploadState(UploadState.ERROR);
					importJob.setErrorMessage("Error: could not create manufacturerModel.");
					logger.error(importJob.getErrorMessage());
					return false;
				}
				equipment = ImportUtils.createEquipment(center, manufacturerModel, deviceSerialNumber);
				if (equipment == null) {
					uploadJob.setUploadState(UploadState.ERROR);
					importJob.setErrorMessage("Error: could not create equipment.");
					logger.error(importJob.getErrorMessage());
					return false;
				} else {
					acquisitionEquipments.add(equipment); // add in memory to avoid loading from server
				}
				studyCard = ImportUtils.createStudyCard(studyREST, equipment, importJob);
				studyCards.add(studyCard); // add in memory to avoid loading from server
			}
		}

		if (studyCard == null) {
			this.importFromTableWindow.error.setText(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
			uploadJob.setUploadState(UploadState.ERROR);
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
			logger.error(importJob.getErrorMessage());
			return false;
		} else {
			if (equipment != null) {
				studyCard.setAcquisitionEquipment(equipment);
			}
			importJob.setStudyCardId(studyCard.getId());
			importJob.setStudyCardName(studyCard.getName());
		}

		logger.info("5. Create subject or use existing one (add subject-study, if necessary)");
		org.shanoir.uploader.model.rest.Subject subjectREST = null;
		String subjectStudyIdentifier = null;
		// Profile Neurinfo/dev: SHANOIR_SUBJECT_NAME column is mandatory
		if (ShUpConfig.isModeSubjectCommonNameManual()) {
			try {
				List<org.shanoir.uploader.model.rest.Subject> existingSubjects = shanoirUploaderServiceClientNG.findSubjectsByStudy(studyREST.getId());
				if (existingSubjects != null) {
					subjectREST = existingSubjects.stream()
						.filter(existingSubject -> importJob.getSubjectName().equals(existingSubject.getName()))
						.findFirst()
						.orElse(null);
					if (subjectREST != null) {
						subjectStudyIdentifier = subjectREST.getName();
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			if (importJob.getSubjectName() == null || importJob.getSubjectName().isBlank()) {
				uploadJob.setUploadState(UploadState.ERROR);
				importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.subject"));
				logger.error(importJob.getErrorMessage());
				return false;
			}
		// Profile OFSEP: SHANOIR_SUBJECT_NAME column is ignored
		} else {
			try {
				subjectREST = shanoirUploaderServiceClientNG.findSubjectBySubjectIdentifier(subject.getIdentifier());
				// If the name does not match, change the subjectStudyIdentifier for this study
				if (subjectREST != null && !subjectREST.getName().equals(importJob.getSubjectName())) {
					subjectStudyIdentifier = importJob.getSubjectName();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return false;
			}	
		}
		subjectREST = ImportUtils.manageSubject(subjectREST,
			subject, importJob.getSubjectName(), ImagedObjectCategory.LIVING_HUMAN_BEING,
			HemisphericDominance.Left.toString(), HemisphericDominance.Left.toString(),
			null, SubjectType.PATIENT, false, false, subjectStudyIdentifier, studyREST, studyCard);
		if (subjectREST == null) {
				uploadJob.setUploadState(UploadState.ERROR);
				importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.subject"));
				logger.error(importJob.getErrorMessage());
				return false;
		}
		importJob.setSubjectName(subjectREST.getName());

		logger.info("6.1 Search existing examinations for subject: a) same date: user has to finish import b) new date: create examination.");
		try {
			List<Examination> examinations = shanoirUploaderServiceClientNG.findExaminationsBySubjectId(subjectREST.getId());
			if (examinations != null && !examinations.isEmpty()) {
				List<Examination> examinationsFilteredByStudy = examinations.parallelStream()
					.filter(e -> e.getStudyId().equals(studyREST.getId()))
					.collect(Collectors.toList());
				for (Iterator iterator = examinationsFilteredByStudy.iterator(); iterator.hasNext();) {
					Examination examination = (Examination) iterator.next();
					// Existing exam found with the same study date: stop importJob and take next one
					Date examinationDate = examination.getExaminationDate();
					LocalDate examinationLocalDate = examinationDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
					if (examinationLocalDate.equals(studyDate)) {
						logger.info("Import job only downloaded, manual user decision needed: existing examination with the same date.");
						return false;
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		logger.info("6.2 Create examination.");
		Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date studyDateDate = Date.from(studyDateInstant);
		Long centerId = studyCard.getAcquisitionEquipment().getCenter().getId();
		Long examinationId = ImportUtils.createExamination(studyREST, subjectREST, studyDateDate, studyDescription, centerId);
		if (examinationId == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.examination"));
			logger.error(importJob.getErrorMessage());
			return false;
		}
		importJob.setExaminationId(examinationId);

		logger.info("7. Prepare uploadJob in thread: pseudonymize DICOM files, write import-job.json and upload-job.xml.");
		importJob.setDicomQuery(null); // clean up, as not necessary anymore
		importJob.setPatientVerification(null); // avoid sending patient info to server
		ImportUtils.prepareImportJob(importJob, subjectREST.getName(), subjectREST.getId(), examinationId, studyREST, studyCard);
		Runnable importRunnable = new ImportFinishRunnable(uploadJob, uploadJobFile.getParentFile(), importJob, subjectREST.getName());
		Thread importThread = new Thread(importRunnable);
		importThread.start();
		while (importThread.isAlive()) {
			// wait for import thread to finish 
		}
		return true;
	}
	
	private boolean queryPacs(ImportJob importJob) {
		logger.info("1. Query PACS with DicomQuery: " + importJob.getDicomQuery().toString());
		List<Patient> patients;
		try {
			patients = dicomServerClient.queryDicomServer(importJob.getDicomQuery());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}
		if (patients == null || patients.isEmpty()) {
			logger.warn("No patients found for DicomQuery.");
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		} else {
			logger.info(patients.size() + " patient(s) found for DicomQuery.");
			if (patients.size() > 1) {
				logger.warn("Attention: multiple patients found for DicomQuery.");
			}
		}
		importJob.setPatients(patients);
		return true;
	}
	
	private LocalDate determineMinDate(ImportJob importJob) {
		LocalDate minDate;
		if (!StringUtils.isBlank(importJob.getDicomQuery().getMinStudyDateFilter())) {
			String[] acceptedFormats = {"yyyy","yyyy-MM-dd","yyyy-MM-dd-HH"};
			try {
				minDate = LocalDate.from(DateUtils.parseDate(importJob.getDicomQuery().getMinStudyDateFilter(), acceptedFormats).toInstant());
			} catch (Exception e) {
				importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.date.format"));
				throw new IllegalArgumentException("Invalid date format");
			}
		} else {
			Instant instant = Instant.now().minusSeconds(20L * 365 * 24 * 60 * 60);
			ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
			minDate = zonedDateTime.toLocalDate();
		}
		return minDate;
	}
	
	private boolean selectPatientStudyAndSeries(ImportJob importJob, LocalDate minDate) {
		logger.info("2. Select patient, study and series");
		List<Serie> selectedSeries = new ArrayList<>();
		Map<Study, List<Serie>> selectedSeriesByStudy = new HashMap<>();
		boolean foundPatient = false;
		LocalDate currentDate = LocalDate.now();
		for (Patient patient : importJob.getPatients()) {
			if (foundPatient) {
				break;
			} else {
				// set patient, and keep if found
				importJob.setPatient(patient);
			}
			for (Study study : patient.getStudies()) {
				LocalDate studyDate = study.getStudyDate();
				if (studyDate.isAfter(currentDate) || studyDate.isBefore(minDate)) {
					continue;
				}
				// filter by study description
				if (!searchField(study.getStudyDescription(), importJob.getDicomQuery().getStudyFilter())) {
					continue;
				}
				Study dicomStudy = study;
				importJob.setStudy(dicomStudy);
				selectedSeriesByStudy.put(dicomStudy, new ArrayList<>());
				for (Serie serie : study.getSeries()) {
					if (searchField(serie.getSeriesDescription(), importJob.getDicomQuery().getSerieFilter())) {
						selectedSeriesByStudy.get(dicomStudy).add(serie);
						foundPatient = true;
						currentDate = studyDate;
					}
				}
			}
		}
		if (selectedSeriesByStudy.isEmpty()) {
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}
		selectedSeries = selectedSeriesByStudy.get(importJob.getStudy());
		if (selectedSeries == null || selectedSeries.isEmpty()) {
			logger.error("No series found for DICOM study: " + importJob.getStudy().toString());
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}
		importJob.setSelectedSeries(new LinkedHashSet<>(selectedSeries));
		return true;
	}

	/**
	 * This method allows to check if a filter with potentiel wildcard '*' is contained in the searched element
	 * @param searchedElement the string that is checked
	 * @param filter the filter we want to find
	 * @return true if the filter matches, false otherwise
	 */
	protected boolean searchField(String searchedElement, String filter) {
		if (StringUtils.isBlank(searchedElement)) {
			return false;
		}
		if (StringUtils.isBlank(filter) || filter.equals("*")) {
			return true;
		}
		String[] filters;
		boolean valid = true;
		// NB: It is possible to have AND ";" filters OR OR ";;" filters but not both at the same time for the moment.

		if (filter.contains(";;")) {
			valid = false;
			filters = filter.split(";;");
			for (String filterToApply : filters) {
				if (filterToApply.startsWith("!")) {
					valid =  valid || !filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				} else {
					valid =  valid || filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				}
			}
			return valid;
		} else if (filter.contains(";")) {
			filters = filter.split(";");
			for (String filterToApply : filters) {
				if (filterToApply.startsWith("!")) {
					valid =  valid && !filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				} else {
					valid =  valid && filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				}
			}
			return valid;
		} else {
			if (filter.startsWith("!")) {
				valid = !filterWildCard(searchedElement, filter.replaceAll("!", ""));
			} else {
				valid = filterWildCard(searchedElement, filter.replaceAll("!", ""));
			}
			return valid;
		}
	}

	/*
	@Test
	public void testSearchField() {
		assertFalse(searchField("", ""));
		assertTrue(searchField("tested", ""));
		assertTrue(searchField("tested", "*"));
		assertTrue(searchField("tested", "*sted"));
		assertTrue(searchField("tested", "test*"));
		assertTrue(searchField("tested", "*est*"));
		assertFalse(searchField("tested", "*ast*"));
		assertTrue(searchField("tested", "*st*;*ed"));
		assertTrue(searchField("tested", "*st*;;*ed"));
		assertFalse(searchField("tested", "*sta*;*ed"));
		assertTrue(searchField("tested", "*sta*;;*ed"));
		assertFalse(searchField("tested", "*sta*;*tad*"));
		assertFalse(searchField("tested", "*sta*;;*tad*"));
	}
	*/

	/**
	 * Check if filterd elements contains or not the data sent in argument
	 * @param searchedElement
	 * @param filter
	 * @return
	 */
	private boolean filterWildCard(String searchedElement, String filter) {
		// Set all to uppercase
		searchedElement = searchedElement.toUpperCase();
		if(filter.endsWith(WILDCARD)) {
			if(filter.startsWith(WILDCARD)) {
				// *filter*
				return searchedElement.contains(filter.replaceAll(WILDCARD_REPLACE, "").toUpperCase());
			}
			// filter*
			return searchedElement.startsWith(filter.replaceAll(WILDCARD_REPLACE, "").toUpperCase());
		}
		if(filter.startsWith(WILDCARD)) {
			// *filter
			return searchedElement.endsWith(filter.replaceAll(WILDCARD_REPLACE, "").toUpperCase());
		}
		// filter
		return searchedElement.equalsIgnoreCase(filter);
	}

}
