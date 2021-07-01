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

package org.shanoir.ng.importer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.dataset.modality.BidsDataset;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetDTO;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ProcessedDatasetType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.bids.BidsDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.eeg.EegDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Channel.ChannelType;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.importer.dto.BidsImportJob;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.DatasetImportEmail;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
public class ImporterService {

	private static final Logger LOG = LoggerFactory.getLogger(ImporterService.class);

	private static final String UPLOAD_EXTENSION = ".upload";

	@Value("${datasets-data}")
	private String niftiStorageDir;

	@Autowired
	private ExaminationService examinationService;

	@Autowired
	private ExaminationRepository examinationRepository;

	@Autowired
	private DatasetAcquisitionContext datasetAcquisitionContext;

	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;

	@Autowired
	private DatasetAcquisitionRepository datasetAcquisitionRepository;

	@Autowired
	private DicomPersisterService dicomPersisterService;

	@Autowired
	private BIDSService bidsService;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	StudyUserRightsRepository studyUserRightRepo;

	@Autowired
	RabbitTemplate rabbitTemplate;

	private static final String SESSION_PREFIX = "ses-";

	private static final String SUBJECT_PREFIX = "sub-";

	private static final String EEG_PREFIX = "eeg";

	public void createAllDatasetAcquisition(ImportJob importJob, Long userId) throws ShanoirException {

		ShanoirEvent event = importJob.getShanoirEvent();
		event.setMessage("Starting import...");
		eventService.publishEvent(event);
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		try {
			Examination examination = examinationRepository.findOne(importJob.getExaminationId());
			Set<DatasetAcquisition> generatedAcquisitions = new HashSet<>();
			if (examination != null) {
				int rank = 0;
				for (Patient patient : importJob.getPatients()) {
					for (Study study : patient.getStudies()) {
						float progress = 0f;
						for (Serie serie : study.getSeries() ) {
							if (serie.getSelected() != null && serie.getSelected()) {
								DatasetAcquisition acquisition = createDatasetAcquisitionForSerie(serie, rank, examination, importJob);
								if (acquisition != null) {
									generatedAcquisitions.add(acquisition);
								}
								rank++;
							}
							progress += 1f / study.getSeries().size();
							// This message is important for email service
							event.setMessage("Treating serie " + serie.getSeriesDescription()+ " for examination " + importJob.getExaminationId());
							event.setProgress(progress);
							eventService.publishEvent(event);
						}
					}
				}
			} else {
				throw new ShanoirException("Examination not found: " + importJob.getExaminationId());
			}

			event.setProgress(1f);
			event.setStatus(ShanoirEvent.SUCCESS);
			// This message is important for email service
			event.setMessage(importJob.getStudyName() + "(" + importJob.getStudyId() + ")"
					+": Successfully created datasets for subject " + importJob.getSubjectName()
					+ " in examination " + examination.getId());
			eventService.publishEvent(event);

			// Send mail
			sendImportEmail(importJob, userId, examination, generatedAcquisitions);

			// Create BIDS folder
			try {
				bidsService.addDataset(examination, importJob.getSubjectName(), importJob.getStudyName());
			} catch (Exception e2) {
				// Only log exception, don't fail for the moment
				event.setMessage("Almost success - BIDS folder creation failed");
				LOG.error("ERROR: Could not create BIDS folder", e2);
			}
			// Manage archive
			if (importJob.getArchive() == null) {
				return;
			}
			// Copy archive
			File archiveFile = new File(importJob.getArchive());
			if (!archiveFile.exists()) {
				LOG.info("Archive file not found, not saved: {}", importJob.getArchive());
				return;
			}
			MultipartFile multipartFile = new MockMultipartFile(archiveFile.getName(), archiveFile.getName(), "application/zip", new FileInputStream(archiveFile));

			// Add bruker archive as extra data
			String fileName = this.examinationService.addExtraData(importJob.getExaminationId(), multipartFile);
			if (fileName != null) {
				List<String> archives = examination.getExtraDataFilePathList();
				if (archives == null) {
					archives = new ArrayList<>();
				}
				archives.add(archiveFile.getName());
				examination.setExtraDataFilePathList(archives);
				examinationRepository.save(examination);
			}
		} catch (Exception e) {
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("Unexpected error during the import: " + e.getMessage() + ", please contact an administrator.");
			event.setProgress(1f);
			eventService.publishEvent(event);
			LOG.error("Error during import for exam: {} : {}", importJob.getExaminationId(), e);
			throw new ShanoirException(event.getMessage(), e);
		}
	}

	/**
	 * Sens the import email through rabbitMQ to user MS
	 * @param importJob the import job
	 * @param userId the userID
	 * @param examination the exam ID
	 * @param generatedAcquisitions
	 */
	private void sendImportEmail(ImportJob importJob, Long userId, Examination examination, Set<DatasetAcquisition> generatedAcquisitions) {
		DatasetImportEmail generatedMail = new DatasetImportEmail();
		generatedMail.setExamDate(examination.getExaminationDate().toString());
		generatedMail.setExaminationId(examination.getId().toString());
		generatedMail.setStudyId(importJob.getStudyId().toString());
		generatedMail.setSubjectName(importJob.getSubjectName());
		generatedMail.setStudyName(importJob.getStudyName());
		generatedMail.setUserId(userId);
		generatedMail.setStudyCard(importJob.getStudyCardName());

		Map<Long, String> datasets = new HashMap<>();
		if (CollectionUtils.isEmpty(generatedAcquisitions)) {
			return;
		}

		for (DatasetAcquisition acq : generatedAcquisitions) {
			if (!CollectionUtils.isEmpty(acq.getDatasets())) {
				for (Dataset dataset : acq.getDatasets()) {
					datasets.put(dataset.getId(), dataset.getName());
				}
			}
		}

		generatedMail.setDatasets(datasets);
		List<Long> recipients = new ArrayList<>();

		// Get all recpients
		List<StudyUser> users = (List<StudyUser>) studyUserRightRepo.findByStudyId(importJob.getStudyId());
		for (StudyUser user : users) {
			if (user.isReceiveNewImportReport()) {
				recipients.add(user.getUserId());
			}
		}
		if (recipients.isEmpty()) {
			// Do not send any mail if no recpients
			return;
		}
		generatedMail.setRecipients(recipients);

		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.IMPORT_DATASET_MAIL_QUEUE, new ObjectMapper().writeValueAsString(generatedMail));
		} catch (AmqpException | JsonProcessingException e) {
			LOG.error("Could not send email for this import. ", e);
		}
	}

	public DatasetAcquisition createDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination, ImportJob importJob) throws Exception {
		if (checkSerieForDicomImages(serie)) {
			datasetAcquisitionContext.setDatasetAcquisitionStrategy(serie.getModality());
			DatasetAcquisition datasetAcquisition = datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, rank, importJob);
			datasetAcquisition.setExamination(examination);
			// TODO: put studyCard in bruker import
			if (datasetAcquisition.getAcquisitionEquipmentId() == null) {
				datasetAcquisition.setAcquisitionEquipmentId(importJob.getAcquisitionEquipmentId());
			}
			// Persist Serie in Shanoir DB
			datasetAcquisitionService.create(datasetAcquisition);
			long startTime = System.currentTimeMillis();
			// Persist Dicom images in Shanoir Pacs
			dicomPersisterService.persistAllForSerie(serie);
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			LOG.info("Import of " + serie.getImagesNumber() + " DICOM images into the PACS required "
					+ duration + " millis for serie: " + serie.getSeriesInstanceUID()
					+ "(" + serie.getSeriesDescription() + ")");
			return datasetAcquisition;
		} else {
			LOG.warn("Serie " + serie.getSequenceName() + ", " + serie.getProtocolName() + " found without images. Ignored.");
		}
		return null;
	}

	/**
	 * Added Temporary check on serie in order not to generate dataset acquisition for series without images.
	 * 
	 * @param serie
	 * @return
	 */
	private boolean checkSerieForDicomImages(Serie serie) {
		return serie.getModality() != null
				&& serie.getDatasets() != null
				&& !serie.getDatasets().isEmpty()
				&& serie.getDatasets().get(0).getExpressionFormats() != null
				&& !serie.getDatasets().get(0).getExpressionFormats().isEmpty()
				&& serie.getDatasets().get(0).getExpressionFormats().get(0).getDatasetFiles() != null
				&& !serie.getDatasets().get(0).getExpressionFormats().get(0).getDatasetFiles().isEmpty();
	}


	public void cleanTempFiles(String workFolder) {
		if (workFolder != null) {
			// delete workFolder.upload file
			File uploadZipFile = new File(workFolder.concat(UPLOAD_EXTENSION));
			uploadZipFile.delete();
			// delete workFolder
			final boolean success = Utils.deleteFolder(new File(workFolder));
			if (!success) {
				if (new File(workFolder).exists()) {
					LOG.error("cleanTempFiles: " + workFolder + " could not be deleted" );
				} else {
					LOG.error("cleanTempFiles: " + workFolder + " does not exist" );
				}
			}
		} else {
			LOG.error("cleanTempFiles: workFolder is null");
		}
	}
	/**
	 * Create a dataset acquisition, and associated dataset.
	 * @param importJob the import job from importer MS.
	 */
	public void createEegDataset(final EegImportJob importJob) {

		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), KeycloakUtil.getTokenUserId(), "Starting import...", ShanoirEvent.IN_PROGRESS, 0f);
		eventService.publishEvent(event);

		if (importJob == null || importJob.getDatasets() == null || importJob.getDatasets().isEmpty()) {
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("No datasets to create. Please check your EEG files");
			event.setProgress(1f);
			eventService.publishEvent(event);
			return;
		}

		try {
			DatasetAcquisition datasetAcquisition = new EegDatasetAcquisition();

			// Get examination
			Examination examination = examinationService.findById(importJob.getExaminationId());

			datasetAcquisition.setExamination(examination);
			datasetAcquisition.setAcquisitionEquipmentId(importJob.getAcquisitionEquipmentId());

			List<Dataset> datasets = new ArrayList<>();
			float progress = 0f;

			for (EegDatasetDTO datasetDto : importJob.getDatasets()) {
				progress += 1f / importJob.getDatasets().size();
				event.setMessage("Dataset " + datasetDto.getName() + " for examination " + importJob.getExaminationId());
				event.setProgress(progress);
				eventService.publishEvent(event);
				// Metadata
				DatasetMetadata originMetadata = new DatasetMetadata();
				originMetadata.setProcessedDatasetType(ProcessedDatasetType.NONRECONSTRUCTEDDATASET);
				originMetadata.setDatasetModalityType(DatasetModalityType.EEG_DATASET);
				originMetadata.setName(datasetDto.getName());
				originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);

				// Create the dataset with informations from job
				EegDataset datasetToCreate = new EegDataset();

				// DatasetExpression with list of files
				DatasetExpression expression = new DatasetExpression();
				expression.setCreationDate(LocalDateTime.now());
				expression.setDatasetExpressionFormat(DatasetExpressionFormat.EEG);
				expression.setDataset(datasetToCreate);

				List<DatasetFile> files = new ArrayList<>();

				// Set files
				if (datasetDto.getFiles() != null) {

					// Copy the data somewhere else
					final String subLabel = SUBJECT_PREFIX + importJob.getSubjectName();
					final String sesLabel = SESSION_PREFIX + importJob.getExaminationId();

					final File outDir = new File(niftiStorageDir + File.separator + EEG_PREFIX + File.separator + subLabel + File.separator + sesLabel + File.separator);
					outDir.mkdirs();

					// Move file one by one to the new directory
					for (String filePath : datasetDto.getFiles()) {

						File srcFile = new File(filePath);
						String originalNiftiName = srcFile.getAbsolutePath().substring(filePath.lastIndexOf('/') + 1);
						File destFile = new File(outDir.getAbsolutePath() + File.separator + originalNiftiName);
						Path finalLocation = null;
						try {
							finalLocation = Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							LOG.error("IOException generating EEG Dataset Expression", e);
						}

						// Create datasetExpression => Files
						if (finalLocation != null) {
							DatasetFile file = new DatasetFile();
							file.setDatasetExpression(expression);
							file.setPath(finalLocation.toUri().toString());
							file.setPacs(false);
							files.add(file);
						}
					}
				}

				expression.setDatasetFiles(files);
				datasetToCreate.setDatasetExpressions(Collections.singletonList(expression));

				// set the dataset_id where needed
				for (Channel chan : datasetDto.getChannels()) {
					chan.setDataset(datasetToCreate);
					chan.setReferenceType(ChannelType.EEG);
					// Parse channel name to get its type
					for (ChannelType type : ChannelType.values()) {
						if (chan.getName().contains(type.name())) {
							chan.setReferenceType(type);
						}
					}
				}
				for (Event eventToImport : datasetDto.getEvents()) {
					eventToImport.setDataset(datasetToCreate);
				}

				// Fill dataset with informations
				datasetToCreate.setChannelCount(datasetDto.getChannels() != null? datasetDto.getChannels().size() : 0);
				datasetToCreate.setChannels(datasetDto.getChannels());
				datasetToCreate.setEvents(datasetDto.getEvents());
				datasetToCreate.setCreationDate(LocalDate.now());
				datasetToCreate.setDatasetAcquisition(datasetAcquisition);
				datasetToCreate.setOriginMetadata(originMetadata);
				//datasetToCreate.setStudyId(importJob.getStudyId());
				datasetToCreate.setSubjectId(importJob.getSubjectId());
				datasetToCreate.setSamplingFrequency(datasetDto.getSamplingFrequency());
				datasetToCreate.setCoordinatesSystem(datasetDto.getCoordinatesSystem());

				datasets.add(datasetToCreate);
			}

			datasetAcquisition.setDatasets(datasets);
			datasetAcquisitionService.create(datasetAcquisition);

			event.setStatus(ShanoirEvent.SUCCESS);
			event.setMessage("Success");
			event.setProgress(1f);
			eventService.publishEvent(event);
			// Complete BIDS with data
			try {
				bidsService.addDataset(examination, importJob.getSubjectName(), importJob.getStudyName());
			} catch (Exception e) {
				LOG.error("Something went wrong creating the bids data: ", e);
			}
		} catch (Exception e) {
			LOG.error("Error while importing EEG: ", e);
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("An unexpected error occured, please contact an administrator.");
			event.setProgress(1f);
			eventService.publishEvent(event);
			throw e;
		}
	}

	/**
	 * Create BIDS dataset.
	 * @param importJob the import job
	 * @param userId the user id
	 */
	public void createAllBidsDatasetAcquisition(BidsImportJob importJob, Long userId) {
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), userId, "Starting import...", ShanoirEvent.IN_PROGRESS, 0f);
		eventService.publishEvent(event);

		try {
			DatasetAcquisition datasetAcquisition = new BidsDatasetAcquisition();

			// Get examination
			Examination examination = examinationRepository.findOne(importJob.getExaminationId());

			datasetAcquisition.setExamination(examination);

			File[] niftisToImport = new File(importJob.getWorkFolder()).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".nii");
				}
			});

			List<Dataset> datasets = new ArrayList<>();
			float progress = 0f;

			for (File niftiFile : niftisToImport) {
				progress += 1f / niftisToImport.length;
				event.setMessage("Bids dataset for examination " + importJob.getExaminationId());
				event.setProgress(progress);
				eventService.publishEvent(event);

				// Metadata
				DatasetMetadata originMetadata = new DatasetMetadata();
				originMetadata.setProcessedDatasetType(ProcessedDatasetType.NONRECONSTRUCTEDDATASET);
				originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
				originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);

				// Create the dataset with informations from job
				BidsDataset datasetToCreate = new BidsDataset();
				datasetToCreate.setSubjectId(examination.getSubjectId());
				datasetToCreate.setModality(importJob.getModality());

				// DatasetExpression with list of files
				DatasetExpression expression = new DatasetExpression();
				expression.setCreationDate(LocalDateTime.now());
				expression.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
				expression.setDataset(datasetToCreate);

				List<DatasetFile> files = new ArrayList<>();

				// Copy the data to BIDS folder
				final String subLabel = SUBJECT_PREFIX + importJob.getSubjectName();
				final String sesLabel = SESSION_PREFIX + importJob.getExaminationId();

				final File outDir = new File(niftiStorageDir + File.separator + File.separator + subLabel + File.separator + sesLabel + File.separator + importJob.getModality());
				outDir.mkdirs();
				String outPath = outDir.getAbsolutePath() + File.separator + niftiFile.getName();
				Path niftiFinalLocation = Files.copy(niftiFile.toPath(), Paths.get(outPath), StandardCopyOption.REPLACE_EXISTING);
				
				DatasetFile dsFile = new DatasetFile();
				dsFile.setDatasetExpression(expression);
				dsFile.setPacs(false);
				dsFile.setPath(niftiFinalLocation.toUri().toString().replaceAll(" ", "%20"));
				files.add(dsFile);
				
				// Add corresponding json if existing
				File jsonFile = new File(niftiFile.getAbsolutePath().replace(".nii", ".json"));
				if (jsonFile.exists()) {
					String outPathJson = outDir.getAbsolutePath() + File.separator + jsonFile.getName();
					Path niftiFinalLocationJson = Files.copy(jsonFile.toPath(), Paths.get(outPathJson), StandardCopyOption.REPLACE_EXISTING);

					DatasetFile jsonFileDs = new DatasetFile();
					jsonFileDs.setDatasetExpression(expression);
					jsonFileDs.setPacs(false);
					jsonFileDs.setPath(niftiFinalLocationJson.toUri().toString().replaceAll(" ", "%20"));
					files.add(jsonFileDs);
				}

				expression.setDatasetFiles(files);
				datasetToCreate.setDatasetExpressions(Collections.singletonList(expression));

				// Fill dataset with informations
				datasetToCreate.setCreationDate(LocalDate.now());
				datasetToCreate.setDatasetAcquisition(datasetAcquisition);
				datasetToCreate.setOriginMetadata(originMetadata);

				datasets.add(datasetToCreate);
			}

			datasetAcquisition.setDatasets(datasets);
			datasetAcquisition.setAcquisitionEquipmentId(1L);
			datasetAcquisitionRepository.save(datasetAcquisition);

			event.setStatus(ShanoirEvent.SUCCESS);
			event.setMessage("Success");
			event.setProgress(1f);
			eventService.publishEvent(event);

			// Complete BIDS with data
			try {
				bidsService.addDataset(examination, importJob.getSubjectName(), importJob.getStudyName());
			} catch (Exception e) {
				LOG.error("Something went wrong creating the bids data: ", e);
			}
		} catch (Exception e) {
			LOG.error("Error while importing BIDS dataset: ", e);
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("An unexpected error occured, please contact an administrator.");
			event.setProgress(1f);
			eventService.publishEvent(event);
			throw new AmqpRejectAndDontRequeueException(e);
		}

	}
}