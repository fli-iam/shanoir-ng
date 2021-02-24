package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.dicom.model.DicomTreeNode;
import org.shanoir.uploader.ShUpConfig;
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
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectStudy;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.model.rest.importer.ImportJob;
import org.shanoir.uploader.model.rest.importer.Instance;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.utils.Util;
import org.shanoir.util.ShanoirUtil;

public class ImportFromCsvActionListener implements ActionListener {

	ImportFromCSVWindow importFromCSVWindow;
	IDicomServerClient dicomServerClient;
	File shanoirUploaderFolder;

	List<CsvImport> csvImports;
	ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;

	private static Logger logger = Logger.getLogger(ImportFromCsvActionListener.class);

	public ImportFromCsvActionListener(ImportFromCSVWindow importFromCSVWindow, IDicomServerClient dicomServerClient, File shanoirUploaderFolder, ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG) {
		this.importFromCSVWindow = importFromCSVWindow;
		this.dicomServerClient = dicomServerClient;
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Iterate over import to import them one by one
		Set<Long> idList = new HashSet<>();
		Map<String, ArrayList<StudyCard>> studyCardsByStudy = new HashMap<>();

		for (CsvImport importTodo : this.csvImports) {
			idList.add(Long.valueOf(importTodo.getStudyId()));
			studyCardsByStudy.put(importTodo.getStudyId(), new ArrayList<StudyCard>());
		}
		
		List<org.shanoir.uploader.model.rest.Study> studies = shanoirUploaderServiceClientNG.findStudiesNamesAndCenters();

		IdList idealist = new IdList();
		idealist.setIdList(new ArrayList<>(idList));
		List<StudyCard> studyCards = shanoirUploaderServiceClientNG.findStudyCardsByStudyIds(idealist);
		studyCards.stream().forEach( element -> {
			try {
				AcquisitionEquipment acquisitionEquipment = shanoirUploaderServiceClientNG.findAcquisitionEquipmentById(element.getAcquisitionEquipmentId());
				element.setAcquisitionEquipment(acquisitionEquipment);
				element.setCenterId(acquisitionEquipment.getCenter().getId());
			} catch (Exception e1) {
				//TODO: error
				e1.printStackTrace();
			}
			studyCardsByStudy.get(element.getStudyId().toString()).add(element);
		});

		for (CsvImport importTodo : this.csvImports) {
			importData(importTodo, studyCardsByStudy.get(importTodo.getStudyId()), studies.stream().filter(element -> element.getId().toString().equals(importTodo.getStudyId())).findFirst().get());
		}
	}

	/**
	 * Loads data to shanoir NG
	 * @param csvImport the import
	 * @param studyCardsByStudy the list of study
	 * @param study2
	 * @return
	 */
	private void importData(CsvImport csvImport, List<StudyCard> studyCardsByStudy, org.shanoir.uploader.model.rest.Study study2) {

		// Check existence of study / study card
		StudyCard sc = studyCardsByStudy.stream().filter(element -> element.getName().equals(csvImport.getStudyCardName())).findFirst().get();
		if (sc == null) {
			// Set csv import in error and then return
			logger.error("We gat a prwobelm here, the study card is not contained in the study");
			return;
		}

		// Request PACS to check the presence of data
		Media media;
		try {
			media = dicomServerClient.queryDicomServer("DIR OL", "", "", "", null, null);
			System.err.println(media);
		} catch (Exception e) {
			// TODO: Set import in error here and stop import for this particular import
			e.printStackTrace();
			return;
		}

		// TODO: change

		Set<Serie> selectedSeries = new HashSet<>();
		Patient pat = null;
		Study stud = null;
		for (DicomTreeNode item : media.getTreeNodes().values()) {
			if (item instanceof Patient) {
				Patient patient = (Patient) item;
				pat = patient;
				Collection<DicomTreeNode> studies = patient.getTreeNodes().values();
				for (Iterator studiesIt = studies.iterator(); studiesIt.hasNext();) {
					Study study = (Study) studiesIt.next();
					stud = study;
					Collection<DicomTreeNode> series = study.getTreeNodes().values();
					for (Iterator seriesIt = series.iterator(); seriesIt.hasNext();) {
						Serie serie = (Serie) seriesIt.next();
						selectedSeries.add(serie);
					}
				}
			}
		}

		DicomDataTransferObject dicomData = null;
		try {
			dicomData = new DicomDataTransferObject(null, pat, stud);
			// TODO identifierCalculator ?
			dicomData.setSubjectIdentifier(csvImport.getCommonName());
		} catch (Exception e) {
			//TODO stop
			e.printStackTrace();
		}

		File uploadFolder = createUploadFolder(dicomServerClient.getWorkFolder(), dicomData);
		List<String> allFileNames = downloadOrCopyFilesIntoUploadFolder(true, selectedSeries, uploadFolder);

		/**
		 * 2. Fill MRI information into serie from first DICOM file of each serie
		 * This has already been done for CD/DVD import, but not yet here for PACS
		 */
		for (Iterator iterator = selectedSeries.iterator(); iterator.hasNext();) {
			Serie serie = (Serie) iterator.next();
			Util.processSerieMriInfo(uploadFolder, serie);
		}

		/**
		 * 3. Write the UploadJob and schedule upload
		 */
		UploadJob uploadJob = new UploadJob();
		initUploadJob(selectedSeries, dicomData, uploadJob);
		if (allFileNames == null) {
			uploadJob.setUploadState(UploadState.ERROR);
		}
		UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
		uploadJobManager.writeUploadJob(uploadJob);

		/**
		 * 4. Write the NominativeDataUploadJobManager for displaying the download state
		 */
		NominativeDataUploadJob dataJob = new NominativeDataUploadJob();
		initDataUploadJob(selectedSeries, dicomData, dataJob);
		if (allFileNames == null) {
			dataJob.setUploadState(UploadState.ERROR);
		}
		NominativeDataUploadJobManager uploadDataJobManager = new NominativeDataUploadJobManager(
				uploadFolder.getAbsolutePath());
		uploadDataJobManager.writeUploadDataJob(dataJob);
		ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, dataJob);

		logger.info(uploadFolder.getName() + ": finished: " + toString());

		// Copy data from PACS
		//List<String> files = dicomServerClient.retrieveDicomFiles(selectedSeries , uploadFolder);

		// pseudonymize data
		// TODO ?

		// Create subject now that we now that we have the series
		Subject subject = new Subject();
		// TODO: remove getTime()
		subject.setName(csvImport.getCommonName() + new Date().getTime());
		subject.setSex(Sex.valueOf(csvImport.getSex()));
		// TODO: change
		subject.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);

		// TODO: change
		subject.setBirthDate(LocalDate.now());
		
		addSubjectStudy(study2, subject);

		// Get center ID from study card
		Long centerId = sc.getCenterId();
		Subject createdSubjet = shanoirUploaderServiceClientNG.createSubject(subject, true, centerId);

		// Create examination
		Examination examDTO = new Examination();
		examDTO.setCenterId(centerId);
		examDTO.setComment(csvImport.getComment());
		examDTO.setExaminationDate(new Date());
		examDTO.setPreclinical(false);
		examDTO.setStudyId(Long.valueOf(csvImport.getStudyId()));
		examDTO.setSubjectId(createdSubjet.getId());
		Examination createdExam = shanoirUploaderServiceClientNG.createExamination(examDTO);

		/**
		 * 3. Fill import-job.json
		 */
		//Exchange exchange = prepareExchange(mainWindow.importDialog, subject.getName(), subject.getId(), examinationId);
		ImportJob importJob = prepareImportJob(uploadJob, createdSubjet.getName(), createdSubjet.getId(), createdExam.getId(), study2, sc);
		Runnable runnable = new ImportFinishRunnableNG(uploadJob, uploadFolder, importJob, subject.getName());
		Thread thread = new Thread(runnable);
		thread.start();

		// Manage error
	}

	private void addSubjectStudy(final org.shanoir.uploader.model.rest.Study study2, final Subject subject) {
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(new IdName(study2.getId(), study2.getName()));
		subjectStudy.setSubject(new IdName(subject.getId(), subject.getName()));

		subjectStudy.setSubjectType(SubjectType.PATIENT);
		subjectStudy.setPhysicallyInvolved(true);
		if (subject.getSubjectStudyList() == null) {
			subject.setSubjectStudyList(new ArrayList<>());
		}
		subject.getSubjectStudyList().add(subjectStudy);
	}
	
	private File createUploadFolder(final File workFolder, final DicomDataTransferObject dicomData) {
		final String timeStamp = ShanoirUtil.getCurrentTimeStampForFS();
		final String folderName = workFolder.getAbsolutePath() + File.separator + dicomData.getSubjectIdentifier()
		+ "_" + timeStamp;
		File uploadFolder = new File(folderName);
		uploadFolder.mkdirs();
		logger.info("UploadFolder created: " + uploadFolder.getAbsolutePath());
		return uploadFolder;
	}

	/**
	 * Initializes UploadJob object to be written to file system.
	 * 
	 * @param selectedSeries
	 * @param dicomData
	 * @param uploadJob
	 */
	private void initUploadJob(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
			final DicomDataTransferObject dicomData, UploadJob uploadJob) {
		uploadJob.setUploadState(UploadState.READY);
		uploadJob.setUploadDate(ShanoirUtil.formatTimePattern(new Date()));
		/**
		 * Patient level
		 */
		// set hash of subject identifier in any case: pseudonymus mode or not
		uploadJob.setSubjectIdentifier(dicomData.getSubjectIdentifier());

		uploadJob.setPatientSex(dicomData.getSex());
		/**
		 * Study level
		 */
		uploadJob.setStudyInstanceUID(dicomData.getStudyInstanceUID());
		uploadJob.setStudyDate(ShUpConfig.formatter.format(dicomData.getStudyDate()));
		uploadJob.setStudyDescription(dicomData.getStudyDescription());
		/**
		 * Serie level
		 */
		uploadJob.setSeries(selectedSeries);
	}

	/**
	 * Initializes UploadStatusServiceJob object
	 * 
	 */
	private void initDataUploadJob(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
			final DicomDataTransferObject dicomData, NominativeDataUploadJob dataUploadJob) {
		dataUploadJob.setPatientName(dicomData.getFirstName() + " " + dicomData.getLastName());
		dataUploadJob.setPatientPseudonymusHash(dicomData.getSubjectIdentifier());
		dataUploadJob.setStudyDate(ShUpConfig.formatter.format(dicomData.getStudyDate()));
		dataUploadJob.setIPP(dicomData.getIPP());
		Serie firstSerie = selectedSeries.iterator().next();
		dataUploadJob.setMriSerialNumber(firstSerie.getMriInformation().getManufacturer()
				+ "(" + firstSerie.getMriInformation().getDeviceSerialNumber() + ")");
		dataUploadJob.setUploadPercentage("");
		dataUploadJob.setUploadState(UploadState.READY);
	}

	/**
	 * In case of the PACS download the destination folder of the DCM server is set to the uploadFolder.
	 * This means, the DICOM files send from the PACS after the c-move, will directly be stored in the
	 * uploadFolder in the workFolder. Each file has the name of its sopInstanceUID and all files are in
	 * the same folder in the end (no sub-folders involved).
	 * In case of the CD/DVD the CD can contain multiple sub-folders with sub-folders, that are referenced
	 * from the DICOMDIR. Therefore ShUp copies the original DICOM files from their deep location, see array
	 * of Tag.ReferencedFileID to the uploadFolder in a flat way: the uploadFolder does not contain sub-folders.
	 * To avoid overwrites because of the same file name, the original path to the file is used as file name,
	 * separated by "_" underscores.
	 * 
	 * @param isFromPACS
	 * @param selectedSeries
	 * @param uploadFolder
	 * @return
	 */
	private List<String> downloadOrCopyFilesIntoUploadFolder(boolean isFromPACS, Set<Serie> selectedSeries, File uploadFolder) {
		List<String> allFileNames = null;
		allFileNames = dicomServerClient.retrieveDicomFiles(selectedSeries, uploadFolder);
		if(allFileNames != null && !allFileNames.isEmpty()) {
			logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files downloaded from PACS.");
		} else {
			logger.info(uploadFolder.getName() + ": error with download from PACS.");
			return null;
		}
		return allFileNames;
	}
	
	private ImportJob prepareImportJob(UploadJob uploadJob, String subjectName, Long subjectId, Long examinationId, org.shanoir.uploader.model.rest.Study study, StudyCard studyCardSelected) {
		ImportJob importJob = new ImportJob();
		importJob.setFromShanoirUploader(true);
		// handle study and study card, using ImportDialog
		org.shanoir.uploader.model.rest.Study studyShanoir = study;
		importJob.setStudyId(studyShanoir.getId());
		importJob.setStudyName(studyShanoir.getName());
		StudyCard studyCard = studyCardSelected;
		// MS Datasets does only return StudyCard DTOs without IDs, as name is unique
		// see: /shanoir-ng-datasets/src/main/java/org/shanoir/ng/studycard/model/StudyCard.java
		importJob.setStudyCardName(studyCard.getName());
		importJob.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		importJob.setConverterId(studyCard.getNiftiConverterId());
		// handle patient and subject
		org.shanoir.uploader.model.rest.importer.Patient patient = new org.shanoir.uploader.model.rest.importer.Patient();
		patient.setPatientID(uploadJob.getSubjectIdentifier());
		Subject subject = new Subject();
		subject.setId(subjectId);
		subject.setName(subjectName);
		importJob.setSubjectName(subjectName);
		patient.setSubject(subject);
		List<org.shanoir.uploader.model.rest.importer.Patient> patients = new ArrayList<>();
		patients.add(patient);
		importJob.setPatients(patients);
		// handle study dicom == examination in Shanoir
		List<org.shanoir.uploader.model.rest.importer.Study> studiesImportJob = new ArrayList<org.shanoir.uploader.model.rest.importer.Study>();
		org.shanoir.uploader.model.rest.importer.Study studyImportJob = new org.shanoir.uploader.model.rest.importer.Study();
		studiesImportJob.add(studyImportJob);
		patient.setStudies(studiesImportJob);
		importJob.setExaminationId(examinationId);
		// handle series for study
		final Collection<Serie> seriesShUp = uploadJob.getSeries();
		final List<org.shanoir.uploader.model.rest.importer.Serie> seriesImportJob = new ArrayList<org.shanoir.uploader.model.rest.importer.Serie>();
		for (org.shanoir.dicom.importer.Serie serieShUp : seriesShUp){
			org.shanoir.uploader.model.rest.importer.Serie serieImportJob = new org.shanoir.uploader.model.rest.importer.Serie();
			serieImportJob.setSelected(true);
			serieImportJob.setSeriesInstanceUID(serieShUp.getId());
			serieImportJob.setSeriesNumber(serieShUp.getSeriesNumber());
			serieImportJob.setModality(serieShUp.getModality());
			serieImportJob.setProtocolName(serieShUp.getProtocol());
			seriesImportJob.add(serieImportJob);
			List<Instance> instancesImportJob = new ArrayList<Instance>();
			for (String filename : serieShUp.getFileNames()){
				Instance instance = new Instance();
				String[] myStringArray = {filename};
				instance.setReferencedFileID(myStringArray);
				instancesImportJob.add(instance);
			}
			serieImportJob.setInstances(instancesImportJob);
			serieImportJob.setImagesNumber(serieShUp.getFileNames().size());
		}
		studyImportJob.setSeries(seriesImportJob);
		return importJob;
	}


	/**
	 * @return the csvImports
	 */
	public List<CsvImport> getCsvImports() {
		return csvImports;
	}

	/**
	 * @param csvImports the csvImports to set
	 */
	public void setCsvImports(List<CsvImport> csvImports) {
		this.csvImports = csvImports;
	}


}
