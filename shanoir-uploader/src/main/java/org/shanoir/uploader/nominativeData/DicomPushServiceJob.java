package org.shanoir.uploader.nominativeData;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.DownloadOrCopyActionListener;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.utils.FileUtil;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DicomPushServiceJob {

	private static final Logger logger = LoggerFactory.getLogger(DicomPushServiceJob.class);

	private DownloadOrCopyActionListener dOCAL;

	private DicomDirGeneratorService dicomDirGeneratorService = new DicomDirGeneratorService();
	
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

	// We keep it as a List because we can have PACS answers with 2 series, 
	// but the same serie, same SeriesInstanceUID but both with different images to manage.
	private final List<Serie> incomingSeries = new ArrayList<>();

	private static final long JOB_RATE = 3600000L; // 1 hour

	private final String regex = "^[0-9.]+$";

	private final File workFolder = ShUpOnloadConfig.getWorkFolder();

	
	public void setDownloadOrCopyActionListener(MainWindow mainWindow) {
		this.dOCAL = mainWindow.dOCAL;
		this.dicomFileAnalyzer = mainWindow.dicomFileAnalyzer;
	}


    @Scheduled(fixedRate = JOB_RATE)
	public void execute() {
		// We only run the job if a username is set
		// and if there is no download or copy already running
		if (ShUpConfig.username != null && !dOCAL.isRunning()) {
			logger.info("Monitoring of incoming 'DICOM PUSHED' examinations started...");
			// new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
			monitorPushedExaminations(workFolder);
			logger.info("Monitoring of incoming 'DICOM PUSHED' examinations ended...");
		}
	}

	/**
	 * Walk trough all folders within the work folder and check if there are complete exams incoming from a DICOM PUSH
	 * we then create the upload xml files for the upload to be loaded in the table.
	 */
	private void monitorPushedExaminations(File workFolder) {
		List<File> folders = Util.listFolders(workFolder);
		if (!folders.isEmpty()) {
			// We browse the content of the workfolder
			for (File dir : folders) {
				// If there is a directory then its a DICOM study, shanoiruploader json import-job will be at that level
				if (dir.isDirectory() && dir.getName().matches(regex)) {
					File jsonFile = new File(dir.getAbsolutePath() + File.separator + ShUpConfig.IMPORT_JOB_JSON);
					if (!jsonFile.exists()) {
						incomingSeries.clear();
						if (isExamComplete(dir)) {
							logger.info("Complete exam found in folder {}.", dir.getName());
						}	
					}
				}
			}
		}
	}

	private boolean isExamComplete(File folder) {
		// We check for DICOM Series subdirectories
		File[] subdirectories = folder.listFiles(f -> f.isDirectory());
		if (subdirectories != null && subdirectories.length > 0) {
			// We create the dicomAttributes at this level to be able to create the patient and study for all the series
			Attributes dicomAttributes = new Attributes();
			// We create a map to store the SeriesInstanceUID for a list of instance numbers as value
			Map<String, List<Integer>> seriesMap = new HashMap<>();
			for (File subdirectory : subdirectories) {
                File[] dicomFiles = subdirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".dcm"));

        		if (dicomFiles == null || dicomFiles.length == 0) {
            		logger.info("No DICOM files found in {}", folder.getName());
					continue;
        		}

        		String studyUID = null;
				String seriesUID = null;
				dicomAttributes.clear();
		
        		for (File file : dicomFiles) {
            		try (DicomInputStream dis = new DicomInputStream(file)) {
                		dicomAttributes = dis.readDataset();

                		String currentStudyUID = dicomAttributes.getString(Tag.StudyInstanceUID);
                		String currentSeriesUID = dicomAttributes.getString(Tag.SeriesInstanceUID);
                		Integer instanceNumber = dicomAttributes.getInt(Tag.InstanceNumber, 0);

                		if (studyUID == null) {
							// We define the studyInstanceUID
                    		studyUID = currentStudyUID;
                		} else if (!studyUID.equals(currentStudyUID)) {
                    		logger.info("Warning: DICOM files from different studies found in the same folder.");
                    		return false;
                		}

						// We create a new Serie from the first dicom file
						if (seriesUID == null) {
							seriesUID = currentSeriesUID;
							Serie serie = new Serie(dicomAttributes);
							//We set the Institution attributes
							dicomFileAnalyzer.addSeriesCenter(serie, dicomAttributes);
							incomingSeries.add(serie);
							// if we have multiple series in the same folder (is it possible ?), 
						} else if (!seriesUID.equals(currentSeriesUID)) {
							logger.info("Warning: DICOM files from different series found in the same folder.");
							return false;
						}

                		// We store the instance number for each serie
                		seriesMap.computeIfAbsent(seriesUID, k -> new ArrayList<>()).add(instanceNumber);

            		} catch (IOException e) {
                		logger.error("Error reading DICOM file: {}", file.getName());
            		}
        		}
			}
			// We check if every serie is complete
			for (Map.Entry<String, List<Integer>> entry : seriesMap.entrySet()) {
				List<Integer> instances = entry.getValue();
				Collections.sort(instances);
				int expectedSize = instances.get(instances.size() - 1);

				if (instances.size() < expectedSize) {
					logger.debug("DICOM serie {} is incomplete.", entry.getKey());
					return false;
				}		
			}
			logger.info("DICOM study {} is complete.", folder.getName());
			// We create the DICOMDIR file in the exam folder
			File dicomDirFile = new File(folder, ShUpConfig.DICOMDIR);
			try {
				dicomDirGeneratorService.generateDicomDirFromDirectory(dicomDirFile, folder);
				logger.info("DICOMDIR generated in directory: " + folder.getAbsolutePath());
				final DicomDirToModelService dicomDirReader = new DicomDirToModelService();
				List<Patient> patients = dicomDirReader.readDicomDirToPatients(dicomDirFile);
				for (Patient patient : patients) {

					// For safety : in some cases values are not retrieved from dicomdir (could use splitPatientName also)
					if ((patient.getPatientBirthDate() == null || patient.getPatientSex() == null)
					 		&& dicomAttributes.getString(Tag.PatientID).equals(patient.getPatientID())) {
						patient.setPatientBirthDate(DateTimeUtils.dateToLocalDate(dicomAttributes.getDate(Tag.PatientBirthDate)));
						patient.setPatientSex(dicomAttributes.getString(Tag.PatientSex));
					}
					Study study = patient.getStudies().get(0);
					// We map the acquisition equipment data from the series list generated from dicom attributes to the series list generated from dicomdir 
					Map<String, Serie> incomingSeriesMap = incomingSeries.stream().collect(Collectors.toMap(Serie::getSeriesInstanceUID, Function.identity()));

					for (Serie serie : study.getSeries()) {
						Serie matchingSerie = incomingSeriesMap.get(serie.getSeriesInstanceUID());
						if (matchingSerie != null) {
							// We set the equipment for the serie
							serie.setEquipment(matchingSerie.getEquipment());
						}
					}
					prepareImportJob(patient, study, incomingSeries);
				}
			} catch (IOException e) {
				logger.error("Error occured during DICOMDIR creation: " + e.getMessage());
			}
		} else {
			logger.info("No DICOM series folder found in study folder {}", folder.getName());
			return false;
		}
		return true;
	}
	/**
	 * Prepare the import job for the DICOM push identified complete examination
	 * @param patient
	 * @param study
	 * @param completeSeries
	 * @param folder
	 */
	private void prepareImportJob(Patient patient, Study study, List<Serie> completeSeries) throws IOException {
		ImportJob importJob = ImportUtils.createNewImportJob(patient, study);
		try {
			importJob.setSubject(ImportUtils.createSubjectFromPatient(patient, dOCAL.pseudonymizer, dOCAL.identifierCalculator));
		} catch (PseudonymusException e) {
			logger.error(e.getMessage(), e);
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			return;
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
			return;
		}

		File uploadFolder = ImportUtils.createUploadFolder(workFolder, importJob.getSubject().getIdentifier());
		importJob.setWorkFolder(uploadFolder.getAbsolutePath());

		List<String> retrievedDicomFiles = new ArrayList<String>();
		StringBuilder downloadOrCopyReportPerStudy = new StringBuilder();
		FileUtil.readAndCopyDicomFilesToUploadFolder(workFolder, study.getStudyInstanceUID(), completeSeries, uploadFolder, retrievedDicomFiles, downloadOrCopyReportPerStudy);

		// We delete the study folder
		FileUtil.deleteFolderDownloadFromDicomServer(workFolder, study.getStudyInstanceUID(), completeSeries);

		// We set the selected series after the copy of the DICOM files to have the instances set to each serie
		importJob.setSelectedSeries(completeSeries);

		importJob.setTimestamp(System.currentTimeMillis());
		importJob.setUploadState(UploadState.READY);
		importJob.setUploadPercentage("");
		
		// We write the import-job.json file
		NominativeDataImportJobManager importJobManager = new NominativeDataImportJobManager(uploadFolder.getAbsolutePath());
		importJobManager.writeImportJob(importJob);
		// We add the nominative data to current uploads
		ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, importJob);
		logger.info(uploadFolder.getName() + ": finished for DICOM Pushed study: " + importJob.getStudy().getStudyDescription()
							+ ", " + importJob.getStudy().getStudyDate() + " of patient: "
							+ importJob.getPatient().getPatientName());
	}
    
}