/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.shanoir.anonymization.anonymization.AnonymizationServiceImpl;
import org.shanoir.ng.importer.dcm2nii.DatasetsCreatorAndNIfTIConverterService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.query.DicomStoreSCPServer;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.Image;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This class actually does the import work and introduces the asynchronous
 * aspect into the import, that the ImporterApiController can directly answer.
 * 
 * @author mkain
 *
 */
@Service
public class ImporterManagerService {

	private static Logger LOG = LoggerFactory.getLogger(ImporterManagerService.class);

	private static final SecureRandom RANDOM = new SecureRandom();
	
	/**
	 * For the moment Spring is not used here to autowire, as we try to keep the
	 * anonymization project as simple as it is, without Spring annotations, to
	 * be usable outside a Spring context.
	 * Maybe to change and think about deeper afterwards.
	 */
	private static final AnonymizationServiceImpl ANONYMIZER = new AnonymizationServiceImpl();
	
	@Autowired
	private QueryPACSService queryPACSService;
	
	@Autowired
	private DicomStoreSCPServer dicomStoreSCPServer;
	
	@Autowired
	private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;
		
	@Autowired
	private DatasetsCreatorAndNIfTIConverterService datasetsCreatorAndNIfTIConverter;
	
	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ShanoirEventService eventService;

	@Value("${shanoir.import.directory}")
	private String importDir;
	
	@Async("asyncExecutor")
	public void manageImportJob(final Long userId, final HttpHeaders keycloakHeaders, final ImportJob importJob) {
		LOG.info("Starting import job for userId: {} with import job folder: {}", userId, importJob.getWorkFolder());
		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), userId, "Starting import configuration", ShanoirEvent.IN_PROGRESS, 0f);
		eventService.publishEvent(event);
		importJob.setShanoirEvent(event);
		try {
			// Always create a userId specific folder in the import work folder (the root of everything):
			// split imports to clearly separate them into separate folders for each user
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs(); // create if not yet existing, e.g. in case of PACS import
			}
			List<Patient> patients = importJob.getPatients();
			// In PACS import the dicom files are still in the PACS, we have to download them first
			// and then analyze them: what gives us a list of images for each serie.
			final File importJobDir;
			if (importJob.isFromPacs()) {
				importJobDir = createImportJobDir(userImportDir.getAbsolutePath());
				// at first all dicom files arrive normally in /tmp/shanoir-dcmrcv (see config DicomStoreSCPServer)
				downloadAndMoveDicomFilesToImportJobDir(importJobDir, patients);
				// convert instances to images, as already done after zip file upload
				imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, importJobDir.getAbsolutePath(), true);
			} else if (importJob.isFromShanoirUploader()) {
				importJobDir = new File(importJob.getWorkFolder());
				// convert instances to images, as already done after zip file upload
				imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, importJobDir.getAbsolutePath(), false);
			} else if (importJob.isFromDicomZip()) {
				// images creation and analyze of dicom files has been done after upload already
				importJobDir = new File(importJob.getWorkFolder());
			} else {
				throw new ShanoirException("Unsupported type of import.");
			}
			event.setMessage("Analyzing series..");
			eventService.publishEvent(event);
			for (Iterator<Patient> patientsIt = patients.iterator(); patientsIt.hasNext();) {
				Patient patient = patientsIt.next();
				// perform anonymization only in case of profile explicitly set
				if (importJob.getAnonymisationProfileToUse() != null && !importJob.getAnonymisationProfileToUse().isEmpty()) {
					ArrayList<File> dicomFiles = getDicomFilesForPatient(importJob, patient, importJobDir.getAbsolutePath());
					final String subjectName = patient.getSubject().getName();
					try {
						ANONYMIZER.anonymizeForShanoir(dicomFiles, importJob.getAnonymisationProfileToUse(), subjectName, subjectName);
					} catch (Exception e) {
						LOG.error(e.getMessage(), e);
						throw new ShanoirException("Error during anonymization.");
					}
				}
				Long converterId = importJob.getConverterId();
				datasetsCreatorAndNIfTIConverter.createDatasetsAndRunConversion(patient, importJobDir, converterId);
			}
	        rabbitTemplate.setBeforePublishPostProcessors(message -> {
	            message.getMessageProperties().setHeader("x-user-id",
	            		KeycloakUtil.getTokenUserId());
	            return message;
	        });
			this.rabbitTemplate.convertAndSend(RabbitMQConfiguration.IMPORTER_QUEUE_DATASET, objectMapper.writeValueAsString(importJob));
		} catch (Exception e) {
			LOG.error("Error during import for study {} and examination {}", importJob.getStudyId(), importJob.getExaminationId(), e);
			event.setMessage("ERROR while importing data for study " + importJob.getStudyId() + " for examination " + importJob.getExaminationId() + ", please contact an administrator");
			event.setStatus(ShanoirEvent.ERROR);
			event.setProgress(1f);
			eventService.publishEvent(event);
		}
		LOG.info("Finished import job for userId: {} with import job folder: {}", userId, importJob.getWorkFolder());
	}
	
	/**
	 * This method creates a random number named work folder to work within during the import.
	 * 
	 * @return file: work folder
	 * @throws ShanoirException
	 */
	private File createImportJobDir(final String parent) throws ShanoirException {
		long n = createRandomLong();
		File importJobDir = new File(parent, Long.toString(n));
		if (!importJobDir.exists()) {
			importJobDir.mkdirs();
		} else {
			throw new ShanoirException("Error while creating importJobDir: folder already exists.");
		}
		return importJobDir;
	}
	
	/**
	 * This method creates a random long number.
	 * 
	 * @return long: random number
	 */
	private long createRandomLong() {
		long n = RANDOM.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}
		return n;
	}

	/**
	 * Calls a c-move for each serie involved, files are received via DicomStoreSCPServer.
	 * 
	 * @param patients
	 * @throws ShanoirException
	 */
	private void downloadAndMoveDicomFilesToImportJobDir(final File importJobDir, List<Patient> patients) throws ShanoirException {
		for (Iterator<Patient> patientsIt = patients.iterator(); patientsIt.hasNext();) {
			Patient patient = patientsIt.next();
			List<Study> studies = patient.getStudies();
			for (Iterator<Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
				Study study = studiesIt.next();
				List<Serie> series = study.getSeries();
				for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
					Serie serie = seriesIt.next();
					queryPACSService.queryCMOVE(serie);
					String serieID = serie.getSeriesInstanceUID();
					File serieIDFolderDir = new File(importJobDir + File.separator + serieID);
					if(!serieIDFolderDir.exists()) {
						serieIDFolderDir.mkdirs();
					} else {
						throw new ShanoirException("Error while creating serie id folder: folder already exists.");
					}
					for (Iterator<Instance> iterator = serie.getInstances().iterator(); iterator.hasNext();) {
						Instance instance = iterator.next();
						String sopInstanceUID = instance.getSopInstanceUID();
						File oldFile = new File(dicomStoreSCPServer.getStorageDirPath() + File.separator + serieID + File.separator + sopInstanceUID + DicomStoreSCPServer.DICOM_FILE_SUFFIX);
						if (oldFile.exists()) {
							File newFile = new File(importJobDir.getAbsolutePath() + File.separator + serieID + File.separator + oldFile.getName());
							oldFile.renameTo(newFile);
							LOG.debug("Moving file: {} to ", oldFile.getAbsolutePath(), newFile.getAbsolutePath());
						} else {
							throw new ShanoirException("Error while creating serie id folder: file to copy does not exist.");
						}
					}
				}
			}
		}
	}

	/**
	 * Using Java HashSet here to avoid duplicate files for anonymization.
	 * For performance reasons already init with 5000 buckets, assuming,
	 * that we will normally never have more than 5000 files to process.
	 * Maybe to be evaluated later with more bigger imports.
	 * 
	 * @param importJob
	 * @param patient
	 * @param workFolderPath
	 * @return list of files
	 * @throws FileNotFoundException
	 */
	private ArrayList<File> getDicomFilesForPatient(final ImportJob importJob, final Patient patient, final String workFolderPath) throws FileNotFoundException {
		Set<File> pathsSet = new HashSet<>(5000);
		List<Study> studies = patient.getStudies();
		for (Iterator<Study> studiesIt = studies.iterator(); studiesIt.hasNext();) {
			Study study = studiesIt.next();
			List<Serie> series = study.getSeries();
			for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
				Serie serie = seriesIt.next();
				handleSerie(workFolderPath, pathsSet, serie);
			}
		}
		return new ArrayList<>(pathsSet);
	}

	/**
	 * This method walks trough the images of a serie, gets the path,
	 * creates a file for it and adds it to pathsSet.
	 * 
	 * @param workFolderPath
	 * @param pathsSet
	 * @param serie
	 * @param importJob
	 * @throws FileNotFoundException
	 */
	private void handleSerie(final String workFolderPath, Set<File> pathsSet, Serie serie) throws FileNotFoundException {
		List<Image> images = serie.getImages();
		for (Iterator<Image> imagesIt = images.iterator(); imagesIt.hasNext();) {
			Image image = imagesIt.next();
			String path = image.getPath();
			File file = new File(workFolderPath + File.separator + path);
			if(file.exists()) {
				pathsSet.add(file);
			} else {
				throw new FileNotFoundException("File not found: " + path);
			}
		}
	}
	
}
