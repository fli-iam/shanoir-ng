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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.shanoir.ng.exchange.imports.subject.IdentifierCalculator;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.PatientVerification;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.utils.Utils;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
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
	private ImportFromTableCSVWriter csvWriter;

	private Pseudonymizer pseudonymizer;
	private IdentifierCalculator identifierCalculator;

	public ImportFromTableRunner(Map<String, ImportJob> importJobs, ResourceBundle ressourceBundle, ImportFromTableWindow importFromTableWindow, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG, Pseudonymizer pseudonymizer) {
		this.importJobs = importJobs;
		this.resourceBundle = ressourceBundle;
		this.importFromTableWindow = importFromTableWindow;
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
		this.pseudonymizer = pseudonymizer;
		this.identifierCalculator = new IdentifierCalculator();
	}

	@Override
	protected Void doInBackground() throws Exception {
		importFromTableWindow.openButton.setEnabled(false);
		importFromTableWindow.uploadButton.setEnabled(false);

		importFromTableWindow.progressBar.setStringPainted(true);
		importFromTableWindow.progressBar.setString("Preparing import...");
		importFromTableWindow.progressBar.setVisible(true);

		logger.info("Preparing import: loading acquisition equipments and add them to study cards");
		org.shanoir.uploader.model.rest.Study studyREST = (org.shanoir.uploader.model.rest.Study) importFromTableWindow.studyCB.getSelectedItem();
		// Important: use all equipments from database here, as they can be used in N studies
		List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClientNG.findAcquisitionEquipments();
		if (acquisitionEquipments == null) {
			// as we create equipments, we can start with an empty list as well
			acquisitionEquipments = new ArrayList<AcquisitionEquipment>();
		}
		if (studyREST.isWithStudyCards()) {
			List<StudyCard> studyCards = studyREST.getStudyCards();
			// as we auto-create new study cards in the process, we can start with an empty list in the study
			if (studyCards == null) {
				studyCards = new ArrayList<StudyCard>();
			}
			for (AcquisitionEquipment acquisitionEquipment : acquisitionEquipments) {
				for (StudyCard studyCard : studyCards) {
					// find the correct equipment for each study card and add it
					if (acquisitionEquipment.getId().equals(studyCard.getAcquisitionEquipmentId())) {
						studyCard.setAcquisitionEquipment(acquisitionEquipment);
					}
				}
			}
		}

		boolean resultAllJobs = true;
		int i = 1;
		csvWriter = new ImportFromTableCSVWriter();
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
				String modality = importJob.getDicomQuery().getModality();
				String importJobIdentifier = "[Line: " + i + ", patientName: " + Utils.sha256(patientName) + ", patientID: " + Utils.sha256(patientID) + ", studyDate: " + studyDate +  ", modality: " + modality + "]";
				logger.info("\r\n------------------------------------------------------\r\n"
					+ "Starting importJob " + importJobIdentifier + "\r\n"
					+ "------------------------------------------------------");
				boolean resultOneJob = importData(importJob, studyREST, acquisitionEquipments, csvWriter);
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

	private boolean importData(ImportJob importJob, org.shanoir.uploader.model.rest.Study studyREST, List<AcquisitionEquipment> acquisitionEquipments, ImportFromTableCSVWriter csvWriter) throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException, InterruptedException {
		PatientVerification patientVerification = importJob.getPatientVerification();
		String[] line = {
			patientVerification.getFirstName(),
			patientVerification.getLastName(),
			patientVerification.getBirthName(),
			patientVerification.getBirthDate(),
			"false",
			importJob.getDicomQuery().getStudyDate(),
			"false"
		};
		if (!queryPacs(importJob)) {
			line[6] = "Not in DICOM server";
			csvWriter.addExaminationLine(false, line);
			return false;
		}
		LocalDate minDate = determineMinDate(importJob);
		if (!selectPatientStudyAndSeries(importJob, minDate)) {
			line[6] = "No DICOM study or series selected in DICOM server";
			csvWriter.addExaminationLine(false, line);
			return false;
		}
		logger.info("DICOM Patient selected: " + importJob.getPatient().toString());
		logger.info("DICOM Study selected: " + importJob.getStudy().toString());
		Patient newPatient = ImportUtils.adjustPatientWithPatientVerification(
			importJob.getPatient(),
			patientVerification.getFirstName(),
			patientVerification.getLastName(),
			patientVerification.getBirthName(),
			patientVerification.getBirthDate());
		importJob.setPatient(newPatient);
		Subject subject = ImportUtils.createSubjectFromPatient(newPatient, pseudonymizer, identifierCalculator);
		importJob.setSubject(subject);

		logger.info("Download from PACS");
		/**
		 * For the moment the ImportFromTableRunner processes line-by-line, DICOM study-by-study,
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
		// Wait for thread to finish
		downloadThread.join();

		EquipmentDicom equipmentDicom = getAndCheckEquipmentDicom(importJob);
		if (equipmentDicom == null) {
			return false;
		}

		logger.info("Find matching study card in selected study or create a new study card");
		StudyCard studyCard = null;
		List<StudyCard> studyCards = studyREST.getStudyCards();
		// Try, if we can find a matching study card already:
		if (studyREST.isWithStudyCards()) {
			if (!studyCards.isEmpty()) {
				// Check if study card configured in Excel: use it (user knows best), no DICOM info necessary
				if (importJob.getStudyCardName() != null && !importJob.getStudyCardName().isEmpty()) {
					Optional<StudyCard> scOpt = studyCards.stream().filter(
						element -> element.getName().equals(importJob.getStudyCardName())).findFirst();
					if (scOpt.isPresent()) {
						studyCard = scOpt.get();
						logger.info("Matching study card found in study by name from table: " + studyCard.getName());
					} else {
						importJob.setUploadState(UploadState.ERROR);
						importJob.setErrorMessage("Error: study card configured in table, but not found in study: " + importJob.getStudyCardName());
						logger.error(importJob.getErrorMessage());
						return false;
					}
				}
				try {
					for (StudyCard studyCardIt : studyCards) {
						if (ImportUtils.flagStudyCardCompatible(studyCardIt, equipmentDicom)) {
							studyCard = studyCardIt;
							logger.info("Matching study card found in study: " + studyCard.getName()
								+ " via manufacturer model name: " + equipmentDicom.getManufacturerModelName()
								+ " and device serial number: " + equipmentDicom.getDeviceSerialNumber());
							break;
						}
					}
				} catch (Exception e) {
					this.importFromTableWindow.error.setText(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
					return false;
				}
			}
		}

		// With or without study card: we might require the creation of
		// a center and/or an acquisition equipment
		Long centerId = null;
		AcquisitionEquipment equipment = null;
		if (studyCard == null) {
			Center center = ImportUtils.findOrCreateCenterWithInstitutionDicom(importJob.getFirstSelectedSerie().getInstitution(), studyREST.getId());
			centerId = center.getId();
			equipment = ImportUtils.findOrCreateEquipmentWithEquipmentDicom(equipmentDicom, center);
			if (equipment != null) {
				acquisitionEquipments.add(equipment);
			} else {
				return false;
			}
			if (studyREST.isWithStudyCards()) {
				studyCard = ImportUtils.createStudyCard(studyREST, equipment);
				studyCards.add(studyCard); // add in memory to avoid loading from server
			}
		} else { // study card used and found
			centerId = studyCard.getCenterId();
			equipment = studyCard.getAcquisitionEquipment();
		}

		if (studyREST.isWithStudyCards()) {
			if (studyCard == null) {
				line[6] = "Error with study card";
				csvWriter.addExaminationLine(false, line);
				this.importFromTableWindow.error.setText(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
				importJob.setUploadState(UploadState.ERROR);
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
		}

		logger.info("Create subject or use existing one (add subject-study, if necessary)");
		org.shanoir.uploader.model.rest.Subject subjectREST = null;
		String subjectStudyIdentifier = null;
		// Profile Neurinfo/dev: in Excel table: SHANOIR_SUBJECT_NAME column is mandatory
		if (ShUpConfig.isModeSubjectNameManual()) {
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
				importJob.setUploadState(UploadState.ERROR);
				importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.subject"));
				logger.error(importJob.getErrorMessage());
				return false;
			}
		// Profile OFSEP: in Excel table: SHANOIR_SUBJECT_NAME column is ignored
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
			null, SubjectType.PATIENT, false, false, subjectStudyIdentifier, studyREST, equipment);
		if (subjectREST == null) {
				line[6] = "Error with subject";
				csvWriter.addExaminationLine(false, line);
				importJob.setUploadState(UploadState.ERROR);
				importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.subject"));
				logger.error(importJob.getErrorMessage());
				return false;
		}
		importJob.setSubjectName(subjectREST.getName());

		logger.info("Search existing examinations for subject: a) same date: user has to finish import b) new date: create examination.");
		line[4] = importJob.getSubjectName();
		line[5] = studyDate.format(DateTimeUtils.FORMATTER);
		try {
			List<Examination> examinations = shanoirUploaderServiceClientNG.findExaminationsBySubjectId(subjectREST.getId());
			if (examinations != null && !examinations.isEmpty()) {
				List<Examination> examinationsFilteredByStudy = examinations.parallelStream()
					.filter(e -> e.getStudyId().equals(studyREST.getId()))
					.collect(Collectors.toList());
				for (Iterator<Examination> iterator = examinationsFilteredByStudy.iterator(); iterator.hasNext();) {
					Examination examination = (Examination) iterator.next();
					// Existing exam found with the same study date: stop importJob and take next one
					Date examinationDate = examination.getExaminationDate();
					LocalDate examinationLocalDate = examinationDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
					if (examinationLocalDate.equals(studyDate)) {
						logger.info("Import job only downloaded, manual user decision needed: existing examination with the same date.");
						csvWriter.addExaminationLine(false, line);
						return false;
					}
				}
			}
		} catch (Exception e) {
			line[6] = e.getMessage();
			csvWriter.addExaminationLine(false, line);
			logger.error(e.getMessage(), e);
			return false;
		}
		logger.info("Create examination.");
		Instant studyDateInstant = studyDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date studyDateDate = Date.from(studyDateInstant);
		// If column SHANOIR_EXAM_COMMENT is not empty we set the examination comment to this value
		if (importJob.getExaminationComment() != null || !importJob.getExaminationComment().isEmpty()) {
			studyDescription = importJob.getExaminationComment();
		}
		Long examinationId = ImportUtils.createExamination(studyREST, subjectREST, studyDateDate, studyDescription, centerId);
		if (examinationId == null) {
			importJob.setUploadState(UploadState.ERROR);
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.examination"));
			line[6] = resourceBundle.getString("shanoir.uploader.import.table.error.examination");
			csvWriter.addExaminationLine(false, line);
			logger.error(importJob.getErrorMessage());
			return false;
		}
		importJob.setExaminationId(examinationId);

		logger.info("Prepare import job in thread: pseudonymize DICOM files, write import-job.json");
		importJob.setDicomQuery(null); // clean up, as not necessary anymore
		importJob.setPatientVerification(null); // avoid sending patient info to server
		ImportUtils.prepareImportJob(importJob, subjectREST.getName(), subjectREST.getId(), examinationId, studyREST, studyCard, equipment);
		File importJobFile = new File(importJob.getWorkFolder() + File.separator + ShUpConfig.IMPORT_JOB_JSON);
		Runnable importRunnable = new ImportFinishRunnable(importJobFile.getParentFile(), importJob, subjectREST.getName());
		Thread importThread = new Thread(importRunnable);
		importThread.start();
		while (importThread.isAlive()) {
			// wait for import thread to finish 
		}
		csvWriter.addExaminationLine(true, line);
		return true;
	}

	private EquipmentDicom getAndCheckEquipmentDicom(ImportJob importJob) {
		EquipmentDicom equipmentDicom = importJob.getFirstSelectedSerie().getEquipment();
		String manufacturerName = equipmentDicom.getManufacturer();
		String manufacturerModelName = equipmentDicom.getManufacturerModelName();
		String deviceSerialNumber = equipmentDicom.getDeviceSerialNumber();
		if (manufacturerName == null || manufacturerName.isBlank()
			|| manufacturerModelName == null || manufacturerModelName.isBlank()
			|| deviceSerialNumber == null || deviceSerialNumber.isBlank()) {
			importJob.setUploadState(UploadState.ERROR);
			importJob.setErrorMessage("Error: no manufacturer or model name or device serial number in DICOM.");
			return null;
		}
		logger.info("Manufacturer name used from DICOM: " + manufacturerName);
		logger.info("Manufacturer model name used from DICOM: " + manufacturerModelName);
		logger.info("Device serial number used from DICOM: " + deviceSerialNumber);
		return equipmentDicom;
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
		Map<String, List<Serie>> selectedSeriesByStudy = new HashMap<>();
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
				// if study dos not contain series, skip it
				if (study.getSeries() == null || study.getSeries().isEmpty()) {
					continue;
				}
				importJob.setStudy(study);
				selectedSeriesByStudy.put(study.getStudyInstanceUID(), new ArrayList<>());
				for (Serie serie : study.getSeries()) {
					if (searchField(serie.getSeriesDescription(), importJob.getDicomQuery().getSerieFilter())) {
						selectedSeriesByStudy.get(study.getStudyInstanceUID()).add(serie);
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
		selectedSeries = selectedSeriesByStudy.get(importJob.getStudy().getStudyInstanceUID());
		// If the dicom study does not contain any series matching the filters
		if (selectedSeries == null || selectedSeries.isEmpty()) {
			logger.error("No series found for DICOM study: " + importJob.getStudy().toString());
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}
		importJob.setSelectedSeries(selectedSeries);
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
