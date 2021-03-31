package org.shanoir.uploader.action;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.dicom.model.DicomTreeNode;
import org.shanoir.ng.exchange.imports.subject.IdentifierCalculator;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.Patient;
import org.shanoir.uploader.dicom.query.Study;
import org.shanoir.uploader.gui.ImportFromCSVWindow;
import org.shanoir.uploader.model.CsvImport;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.shanoir.util.ShanoirUtil;

public class ImportFromCsvRunner extends SwingWorker<Void, Integer> {

	private static final String WILDCARD = "*";
	private static final String WILDCARD_REPLACE = "\\*";

	private static Logger logger = Logger.getLogger(ImportFromCsvRunner.class);

	private List<CsvImport> csvImports;
	private ResourceBundle resourceBundle;
	private ImportFromCSVWindow importFromCSVWindow;
	private IdentifierCalculator identifierCalculator;
	private IDicomServerClient dicomServerClient;
	private ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	public ImportFromCsvRunner(List<CsvImport> csvImports, ResourceBundle ressourceBundle, ImportFromCSVWindow importFromCSVWindow, IDicomServerClient dicomServerClient, ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG) {
		this.csvImports = csvImports;
		this.resourceBundle = ressourceBundle;
		this.importFromCSVWindow = importFromCSVWindow;
		this.identifierCalculator = new IdentifierCalculator();
		this.dicomServerClient = dicomServerClient;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
	}

	@Override
	protected Void doInBackground() throws Exception {
		// Iterate over import to import them one by one
		Set<Long> idList = new HashSet<>();
		Map<String, ArrayList<StudyCard>> studyCardsByStudy = new HashMap<>();

		importFromCSVWindow.openButton.setEnabled(false);
		importFromCSVWindow.uploadButton.setEnabled(false);

		importFromCSVWindow.progressBar.setStringPainted(true);
		importFromCSVWindow.progressBar.setString("Preparing import...");
		importFromCSVWindow.progressBar.setVisible(true);

		// Get the list of studies, study card, center, equipements to check their existence
		for (CsvImport importTodo : this.csvImports) {
			idList.add(Long.valueOf(importTodo.getStudyId()));
			studyCardsByStudy.put(importTodo.getStudyId(), new ArrayList<StudyCard>());
		}
		List<org.shanoir.uploader.model.rest.Study> studies = new ArrayList<>();
		try {
			studies = shanoirUploaderServiceClientNG.findStudiesNamesAndCenters();

			IdList idealist = new IdList();
			idealist.setIdList(new ArrayList<>(idList));
			List<StudyCard> studyCards = shanoirUploaderServiceClientNG.findStudyCardsByStudyIds(idealist);

			if (studyCards == null) {
				throw new ShanoirException(resourceBundle.getString("shanoir.uploader.import.csv.error.studycard"));
			}

			// Iterate over study cards to get equipement + fill study => SC map
			for (StudyCard studyCard : studyCards) {
				AcquisitionEquipment acquisitionEquipment;
				acquisitionEquipment = shanoirUploaderServiceClientNG.findAcquisitionEquipmentById(studyCard.getAcquisitionEquipmentId());
				if (acquisitionEquipment == null) {
					throw new ShanoirException("Error while retrieving study cards");
				}
				studyCard.setAcquisitionEquipment(acquisitionEquipment);
				studyCard.setCenterId(acquisitionEquipment.getCenter().getId());
				studyCardsByStudy.get(studyCard.getStudyId().toString()).add(studyCard);
			}
		} catch (Exception e1) {
			this.importFromCSVWindow.error.setText(resourceBundle.getString("shanoir.uploader.import.csv.error.studycard"));
			return null;
		}

		boolean success = true;
		int i = 1;

		for (CsvImport importTodo : this.csvImports) {
			importFromCSVWindow.progressBar.setString("Preparing import " + i + "/" + this.csvImports.size());
			importFromCSVWindow.progressBar.setValue(100*i/this.csvImports.size() + 1);

			org.shanoir.uploader.model.rest.Study study = studies.stream().filter(element -> element.getId().toString().equals(importTodo.getStudyId())).findFirst().get();
			success = importData(importTodo, studyCardsByStudy.get(importTodo.getStudyId()), study ) && success;
			i++;
		}

		if (success) {
			importFromCSVWindow.progressBar.setString("Success !");
			importFromCSVWindow.progressBar.setValue(100);

			// Open current import tab and close csv import panel
			((JTabbedPane) this.importFromCSVWindow.scrollPaneUpload.getParent().getParent()).setSelectedComponent(this.importFromCSVWindow.scrollPaneUpload.getParent());

			this.importFromCSVWindow.frame.setVisible(false);
			this.importFromCSVWindow.frame.dispose();
		} else {
			this.importFromCSVWindow.displayCsv(csvImports);
			importFromCSVWindow.openButton.setEnabled(true);
			importFromCSVWindow.uploadButton.setEnabled(false);
		}
		return null;
	}

	/**
	 * Loads data to shanoir NG
	 * @param csvImport the import
	 * @param studyCardsByStudy the list of study
	 * @param study2
	 * @return
	 */
	private boolean importData(CsvImport csvImport, List<StudyCard> studyCardsByStudy, org.shanoir.uploader.model.rest.Study study2) {

		// 1. Request PACS to check the presence of data
		logger.info("1 Request PACS");
		Media media;
		try {
			String name = csvImport.getName().toUpperCase();
			if (!StringUtils.isEmpty(csvImport.getSurname())) {
				name+="^";
				name+=csvImport.getSurname().toUpperCase();
			}
			media = dicomServerClient.queryDicomServer(name, "", "", "", null, null);
		} catch (Exception e) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}

		// 2. Select series
		logger.info("2 Select series");

		Set<Serie> selectedSeries = new HashSet<>();
		Patient pat = null;
		Study stud = null;
		if (media == null || media.getTreeNodes() == null || media.getTreeNodes().isEmpty()) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}
		boolean foundPatient = false;
		String serialNumber = null;
		String modelName = null;
		
		Map<Study, Set<Serie>> selectedSeriesByStudy = new HashMap<>();
		Study selectedStudy = null;

		Calendar calendar = Calendar.getInstance();
		if (!StringUtils.isBlank(csvImport.getMinDateFilter())) {
			calendar.set(Integer.valueOf(csvImport.getMinDateFilter()), 0, 1, 0, 0, 0);
		} else {
			calendar.set(1000, 0, 1, 0, 0, 0);
		}
		Date minDate = calendar.getTime();

		for (DicomTreeNode item : media.getTreeNodes().values()) {
			if (foundPatient) {
				// Get only one patient => Once we've selected a serie with interesting data, do not iterate more
				break;
			}
			if (item instanceof Patient) {
				Patient patient = (Patient) item;
				pat = patient;
				Collection<DicomTreeNode> studies = patient.getTreeNodes().values();
				Date currentDate = new Date();
				for (Iterator<DicomTreeNode> studiesIt = studies.iterator(); studiesIt.hasNext();) {
					// Select the first study (comparing dates)
					Study study = (Study) studiesIt.next();
				// get study date
					SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
					Date studyDate = new Date();
					try {
						studyDate = format1.parse(study.getDescriptionMap().get("date"));
					} catch (ParseException e) {
						// Could not get date => skip the study
						continue;
					}
					if (studyDate.after(currentDate) || studyDate.before(minDate)) {
						// We take the first valid date, if we are after on valid date, don't check the data
						continue;
					}
					if (!searchField(study.getDisplayString(), csvImport.getStudyFilter())) {
						continue;
					}
					stud = study;
					selectedSeriesByStudy.put(stud, new HashSet<>());
					Collection<DicomTreeNode> series = study.getTreeNodes().values();
					for (Iterator<DicomTreeNode> seriesIt = series.iterator(); seriesIt.hasNext();) {
						// Filter on serie
						Serie serie = (Serie) seriesIt.next();
						if (searchField(serie.getDescription(), csvImport.getAcquisitionFilter())) {
							selectedSeriesByStudy.get(stud).add(serie);
							selectedStudy = stud;
							serialNumber = serie.getMriInformation().getDeviceSerialNumber();
							modelName = serie.getMriInformation().getManufacturersModelName();
							foundPatient = true;
							currentDate = studyDate;
						}
					}
				}
			}
		}
		if (selectedStudy == null) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}
		selectedSeries = selectedSeriesByStudy.get(selectedStudy);
		if (selectedSeries.isEmpty()) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}

		// 3. Check existence of study / study card
		logger.info("3 Check study card");

		StudyCard sc = null;
		for (StudyCard studyc : studyCardsByStudy) {
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
			Optional<StudyCard> scOpt = studyCardsByStudy.stream().filter(element -> element.getName().equals(csvImport.getStudyCardName())).findFirst();
			if (!scOpt.isPresent()) {
				csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.studycard"));
				return false;
			} else {
				sc = scOpt.get();
			}
		}

		// 4. Create DicomDataTransferObject
		logger.info("4 Complete data");

		DicomDataTransferObject dicomData = null;
		String subjectIdentifier = "";

		try {
			dicomData = new DicomDataTransferObject(null, pat, stud);

			// Calculate identifier
			subjectIdentifier = this.identifierCalculator.calculateIdentifier(dicomData.getFirstName(), dicomData.getLastName(), dicomData.getBirthDate());
			dicomData.setSubjectIdentifier(subjectIdentifier);

			// Change birth date to first day of year
			final String dicomBirthDate = pat.getDescriptionMap().get("birthDate");
			if (!StringUtils.isEmpty(dicomBirthDate)) {
				Date dicomBirthDateAsDate = ShanoirUtil.convertStringDicomDateToDate(dicomBirthDate);
				Calendar cal = Calendar.getInstance();
				cal.setTime(dicomBirthDateAsDate);
				cal.set(Calendar.MONTH, Calendar.JANUARY);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				dicomData.setBirthDate(cal.getTime());
			}
		} catch (Exception e) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}

		File uploadFolder = ImportUtils.createUploadFolder(dicomServerClient.getWorkFolder(), dicomData);
		List<String> allFileNames = ImportUtils.downloadOrCopyFilesIntoUploadFolder(true, selectedSeries, uploadFolder, this.dicomServerClient, null);

		/**
		 * 5. Fill MRI information into serie from first DICOM file of each serie
		 * This has already been done for CD/DVD import, but not yet here for PACS
		 */
		logger.info("5 Fill MRI info");

		for (Iterator<Serie> iterator = selectedSeries.iterator(); iterator.hasNext();) {
			Serie serie = iterator.next();
			Util.processSerieMriInfo(uploadFolder, serie);
		}

		/**
		 * 6. Write the UploadJob and schedule upload
		 */
		logger.info("6 Write upload job");

		UploadJob uploadJob = new UploadJob();
		ImportUtils.initUploadJob(selectedSeries, dicomData, uploadJob);

		if (allFileNames == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.pacs.copy"));
			return false;
		}
		UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
		uploadJobManager.writeUploadJob(uploadJob);

		/**
		 * 7. Write the NominativeDataUploadJobManager for displaying the download state
		 */
		logger.info("7 Write upload job nominative");

		NominativeDataUploadJob dataJob = new NominativeDataUploadJob();
		ImportUtils.initDataUploadJob(selectedSeries, dicomData, dataJob);

		NominativeDataUploadJobManager uploadDataJobManager = new NominativeDataUploadJobManager(
				uploadFolder.getAbsolutePath());
		uploadDataJobManager.writeUploadDataJob(dataJob);
		ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, dataJob);

		logger.info(uploadFolder.getName() + ": finished: " + toString());

		Long centerId = sc.getCenterId();

		// 8.  Create subject if necessary
		Subject subjectFound = null;
		String subjectStudyIdentifier = null;
		try {
			subjectFound = shanoirUploaderServiceClientNG.findSubjectBySubjectIdentifier(subjectIdentifier);
			if (!subjectFound.getName().equals(csvImport.getCommonName())) {
				// If the name does not match, change the subjectStudyIdentifier for this study
				subjectStudyIdentifier = csvImport.getCommonName();
			}
		} catch (Exception e) {
			//Do nothing, if it fails, we'll just create a new subject
		}
		Subject subject;
		if (subjectFound != null) {
			logger.info("8 Subject exists, just use it");
			subject = subjectFound;
			ImportUtils.addSubjectStudy(study2, subject, SubjectType.PATIENT, true, subjectStudyIdentifier);
			subject = shanoirUploaderServiceClientNG.createSubjectStudy(subject);
		} else {
			logger.info("8 Creating a new subject");
	
			subject = new Subject();
			subject.setName(csvImport.getCommonName());
			if (!StringUtils.isEmpty(pat.getDescriptionMap().get("sex"))) {
				subject.setSex(Sex.valueOf(pat.getDescriptionMap().get("sex")));
			} else {
				// Force feminine (girl power ?)
				subject.setSex(Sex.F);
			}
			subject.setIdentifier(subjectIdentifier);
	
			subject.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);
	
			subject.setBirthDate(dicomData.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	
			ImportUtils.addSubjectStudy(study2, subject, SubjectType.PATIENT, true, subjectStudyIdentifier);
	
			// Get center ID from study card
			subject = shanoirUploaderServiceClientNG.createSubject(subject, true, centerId);
		}
		if (subject == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.subject"));
			return false;
		}

		// 9. Create examination
		logger.info("9 Create exam");

		Examination examDTO = new Examination();
		examDTO.setCenterId(centerId);
		examDTO.setComment(csvImport.getComment());
		examDTO.setExaminationDate(new Date());
		examDTO.setPreclinical(false);
		examDTO.setStudyId(Long.valueOf(csvImport.getStudyId()));
		examDTO.setSubjectId(subject.getId());
		Examination createdExam = shanoirUploaderServiceClientNG.createExamination(examDTO);

		if (createdExam == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.examination"));
			return false;
		}

		/**
		 * 10. Fill import-job.json to prepare the import
		 */
		logger.info("10 Import.json");

		ImportJob importJob = ImportUtils.prepareImportJob(uploadJob, subject.getName(), subject.getId(), createdExam.getId(), study2, sc);
		Runnable runnable = new ImportFinishRunnableNG(uploadJob, uploadFolder, importJob, subject.getName());
		Thread thread = new Thread(runnable);
		thread.start();

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
		if (StringUtils.isBlank(filter) || filter.equals(WILDCARD)) {
			return true;
		}
		String[] filters = filter.split(";");
		boolean valid = true;
		for (String filterToApply : filters) {
			// NB: we choose to use AND instead of OF between filters.
			// This way we are more restrictive
			// If you want more acquisitions, you have to duplicate the line in the CSV with other filters

			if (filterToApply.startsWith("!")) {
				valid =  valid && !filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
			} else {
				valid =  valid && filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
			}
		}
		return valid;
	}

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
