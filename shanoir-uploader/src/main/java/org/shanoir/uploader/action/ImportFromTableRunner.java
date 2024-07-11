package org.shanoir.uploader.action;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.Subject;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Sex;
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

		org.shanoir.uploader.model.rest.Study study = (org.shanoir.uploader.model.rest.Study) importFromTableWindow.studyCB.getSelectedItem();
		List<StudyCard> studyCards;
		try {
			IdList idList = new IdList();
			idList.getIdList().add(study.getId());
			studyCards = shanoirUploaderServiceClientNG.findStudyCardsByStudyIds(idList);
			if (studyCards == null) {
				throw new Exception(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
			}
			List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClientNG.findAcquisitionEquipments();
			if (acquisitionEquipments == null) {
				throw new Exception("Error while retrieving acquisition equipments.");
			}
			// Iterate over study cards to get equipment + fill study => SC map
			for (StudyCard studyCard : studyCards) {
				Long acquisitionEquipmentIdSC = studyCard.getAcquisitionEquipmentId();
				for (Iterator<AcquisitionEquipment> acquisitionEquipmentsIt = acquisitionEquipments.iterator(); acquisitionEquipmentsIt.hasNext();) {
					AcquisitionEquipment acquisitionEquipment = (AcquisitionEquipment) acquisitionEquipmentsIt.next();
					if (acquisitionEquipment.getId().equals(acquisitionEquipmentIdSC)) {
						studyCard.setAcquisitionEquipment(acquisitionEquipment);
						studyCard.setCenterId(acquisitionEquipment.getCenter().getId());
						break;
					}
				}
			}
		} catch (Exception e) {
			this.importFromTableWindow.error.setText(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
			return null;
		}

		boolean success = true;
		int i = 1;

		for (ImportJob importJob : importJobs.values()) {
			importFromTableWindow.progressBar.setString("Preparing import " + i + "/" + this.importJobs.size());
			importFromTableWindow.progressBar.setValue(100*i/this.importJobs.size() + 1);
			try {
				success = importData(importJob, studyCards, study ) && success;
			} catch(Exception exception) {
				logger.error(exception.getMessage(), exception);
			}
			i++;
		}

		if (success) {
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
		return null;
	}

	private boolean importData(ImportJob importJob, List<StudyCard> studyCards, org.shanoir.uploader.model.rest.Study studyREST) throws UnsupportedEncodingException, NoSuchAlgorithmException, PseudonymusException {
		logger.info("1. Query PACS");
		List<Patient> patients = null;
		try {
			patients = dicomServerClient.queryDicomServer(importJob.getDicomQuery());
		} catch (Exception e) {
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}

		logger.info("2. Select series");
		List<Serie> selectedSeries = new ArrayList<>();

		Patient pat = null;
		Study stud = null;
		if (patients == null || patients.isEmpty()) {
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}
		String serialNumber = null;
		String modelName = null;
		
		Study selectedStudy = null;
		Map<Study, List<Serie>> selectedSeriesByStudy = new HashMap<>();

		LocalDate minDate;
		LocalDate selectedStudyDate = LocalDate.now();
		if (!StringUtils.isBlank(importJob.getDicomQuery().getMinStudyDateFilter())) {
			String[] acceptedFormats = {"yyyy","yyyy-MM-dd","yyyy-MM-dd-HH"};
			try {
				minDate = LocalDate.from(DateUtils.parseDate(importJob.getDicomQuery().getMinStudyDateFilter(), acceptedFormats).toInstant());
				selectedStudyDate = minDate;
			} catch (Exception e) {
				importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.date.format"));
				return false;
			}
		} else {
			Calendar cal = Calendar.getInstance();
			cal.set(1000, 0, 1, 0, 0, 0);
			minDate = LocalDate.from(cal.toInstant());
		}

		boolean foundPatient = false;
		for (Patient patient : patients) {
			if (foundPatient) {
				// Get only one patient => Once we've selected a serie with interesting data, do not iterate more
				break;
			}

			List<Study> studies = patient.getStudies();
			LocalDate currentDate = LocalDate.now();
			for (Study study : studies) {
				LocalDate studyDate = study.getStudyDate();
				if (studyDate.isAfter(currentDate) || studyDate.isAfter(minDate)) {
					// We take the first valid date, if we are after on valid date, don't check the data
					continue;
				}
				
				if (!searchField(study.getStudyDescription(), importJob.getDicomQuery().getStudyFilter())) {
					continue;
				}
				stud = study;
				pat = patient;
				selectedSeriesByStudy.put(stud, new ArrayList<>());
				Collection<Serie> series = study.getSelectedSeries();
				for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
					// Filter on serie
					Serie serie = seriesIt.next();
					if (searchField(serie.getSeriesDescription(), importJob.getDicomQuery().getSerieFilter())) {
						selectedSeriesByStudy.get(stud).add(serie);
						selectedStudy = stud;
						// TOOD: get these
						serialNumber = serie.getEquipment().getDeviceSerialNumber();
						modelName = serie.getEquipment().getManufacturerModelName();
						foundPatient = true;
						currentDate = studyDate;
						selectedStudyDate = studyDate;
					}
				}
			}
		}

		if (selectedStudy == null) {
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}
		selectedSeries = selectedSeriesByStudy.get(selectedStudy);
		if (selectedSeries.isEmpty()) {
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.missing.data"));
			return false;
		}
		
		/**
		 * For the moment the ImportFromTableRunner processes line-by-line, study-by-study,
		 * so we only send one import job to the DownloadOrCopyRunnable, to download only
		 * one DICOM study, as the code after directly finishes the import of this study.
		 */
		HashMap<String, ImportJob> downloadImportJobs = new HashMap<String, ImportJob>();
		ImportJob downloadImportJob = ImportUtils.createNewImportJob(pat, selectedStudy);
		selectedSeries.stream().forEach(s -> importJob.getSelectedSeries().add(s));
		downloadImportJobs.put(importJob.getStudy().getStudyInstanceUID(), downloadImportJob);
		Runnable downloadRunnable = new DownloadOrCopyRunnable(true, dicomServerClient, dicomFileAnalyzer,  null, downloadImportJobs);
		Thread downloadThread = new Thread(downloadRunnable);
		downloadThread.start();

		// 3. Check existence of study / study card
		logger.info("3. Check study card");
		StudyCard sc = null;
		for (StudyCard studyc : studyCards) {
			if (serialNumber != null && serialNumber.equals(studyc.getAcquisitionEquipment().getSerialNumber())) {
				sc = studyc;
				break;
			}
			if (modelName != null && modelName.equals(studyc.getAcquisitionEquipment().getManufacturerModel().getName())) {
				sc = studyc;
				break;
			}
		}

		// No study card by default => get the one in the file (if existing of course)
		if (sc == null) {
			Optional<StudyCard> scOpt = studyCards.stream().filter(element -> element.getName().equals(importJob.getStudyCardName())).findFirst();
			if (!scOpt.isPresent()) {
				importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.studycard"));
				return false;
			} else {
				sc = scOpt.get();
			}
		}

		logger.info("4. Complete data");
		/**
		 * Possible @todo: add 4 columns to Excel with patient information
		 * to create correct hash here, as verified in the GUI in patient
		 * verification.
		 */
		Subject subject = dOCAL.createSubjectFromPatient(pat);
		Long centerId = sc.getCenterId();

		// 4 Create subject if necessary
		org.shanoir.uploader.model.rest.Subject subjectFound = null;
		String subjectStudyIdentifier = null;
		try {
			subjectFound = shanoirUploaderServiceClientNG.findSubjectBySubjectIdentifier(subject.getIdentifier());
			if (!subjectFound.getName().equals(importJob.getSubjectName())) {
				// If the name does not match, change the subjectStudyIdentifier for this study
				subjectStudyIdentifier = importJob.getSubjectName();
			}
		} catch (Exception e) {
			//Do nothing, if it fails, we'll just create a new subject
		}
		
		if (subjectFound != null) {
			logger.info("5. Subject exists, just use it");
			ImportUtils.addSubjectStudy(studyREST, subjectFound, SubjectType.PATIENT, true, subjectStudyIdentifier);
			subjectFound = shanoirUploaderServiceClientNG.createSubjectStudy(subjectFound);
		} else {
			logger.info("5. Creating a new subject");
			subjectFound = new org.shanoir.uploader.model.rest.Subject();
			subjectFound.setName(importJob.getSubjectName());
			if (!StringUtils.isEmpty(pat.getPatientSex())) {
				subjectFound.setSex(Sex.valueOf(pat.getPatientSex()));
			} else {
				// Force feminine (girl power ?)
				subjectFound.setSex(Sex.F);
			}
			subjectFound.setIdentifier(subject.getIdentifier());
			subjectFound.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);
			subjectFound.setBirthDate(subject.getBirthDate());
			ImportUtils.addSubjectStudy(studyREST, subjectFound, SubjectType.PATIENT, true, subjectStudyIdentifier);
			// Get center ID from study card
			subjectFound = shanoirUploaderServiceClientNG.createSubject(subjectFound, true, centerId);
		}

		File uploadJobFile = new File(importJob.getWorkFolder() + File.separator + UploadJobManager.UPLOAD_JOB_XML);
		UploadJobManager uploadJobManager = new UploadJobManager(uploadJobFile);
		UploadJob uploadJob = uploadJobManager.readUploadJob();

		if (subject == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.subject"));
			return false;
		}

		// 9. Create examination
		logger.info("6. Create exam");

		Examination examDTO = new Examination();
		examDTO.setCenterId(centerId);
		examDTO.setComment(importJob.getExaminationComment());
		examDTO.setExaminationDate(Date.from(selectedStudyDate.atStartOfDay().toInstant(ZoneOffset.UTC)));
		examDTO.setPreclinical(false);
		examDTO.setStudyId(Long.valueOf(importJob.getStudyId()));
		examDTO.setSubjectId(subject.getId());
		Examination createdExam = shanoirUploaderServiceClientNG.createExamination(examDTO);

		if (createdExam == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			importJob.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.table.error.examination"));
			return false;
		}

		logger.info("7. Import.json");
		ImportUtils.prepareImportJob(importJob, subject.getName(), subject.getId(), createdExam.getId(), studyREST, sc);
		Runnable importRunnable = new ImportFinishRunnable(uploadJob, uploadJobFile.getParentFile(), importJob, subject.getName());
		Thread importThread = new Thread(importRunnable);
		importThread.start();

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
