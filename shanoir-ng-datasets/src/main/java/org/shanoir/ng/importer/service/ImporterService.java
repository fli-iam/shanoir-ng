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

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.dataset.modality.*;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.*;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.*;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.studycard.service.QualityCardService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

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
	private DatasetService datasetService;

	@Autowired
	private DatasetAcquisitionService datasetAcquisitionService;

	@Autowired
	private DicomPersisterService dicomPersisterService;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
    private SolrService solrService;

	@Autowired
	private ImporterMailService mailService;

    @Autowired
    private StudyCardRepository studyCardRepository;

    @Autowired
    private QualityCardService qualityCardRepository;

    @Autowired
    private DicomProcessing dicomProcessing;

	private static final String SUBJECT_PREFIX = "sub-";
	
	private static final String PROCESSED_DATASET_PREFIX = "processed-dataset";

	private static int instancesCreated = 0;

    //This constructor will be called everytime a new bean instance is created
    public ImporterService(){
        instancesCreated++;
    }

    public static int getInstancesCreated(){
        return ImporterService.instancesCreated;
    }

	public void createAllDatasetAcquisition(ImportJob importJob, Long userId) throws ShanoirException {
		LOG.info("createAllDatasetAcquisition: " + this + " instances: " + getInstancesCreated());
		ShanoirEvent event = importJob.getShanoirEvent();
		event.setMessage("Creating datasets...");
		eventService.publishEvent(event);
		SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
		try {
			Examination examination = examinationRepository.findById(importJob.getExaminationId()).orElse(null);
			Set<DatasetAcquisition> generatedAcquisitions = new HashSet<>();
			if (examination != null) {
				int rank = 0;
				for (Patient patient : importJob.getPatients()) {
					for (Study study : patient.getStudies()) {
						float progress = 0.5f;
						for (Serie serie : study.getSeries() ) {
							if (serie.getSelected() != null && serie.getSelected()) {
								DatasetAcquisition acquisition = createDatasetAcquisitionForSerie(serie, rank, examination, importJob);
								persistAcquisition(acquisition);
								persistSerieInPacs(serie);
								if (acquisition != null) {
									generatedAcquisitions.add(acquisition);
								}
								rank++;
							}
							progress += 0.5f / study.getSeries().size();
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

			event.setMessage(importJob.getStudyName() + "(" + importJob.getStudyId() + ")"
					+": Successfully created datasets for subject " + importJob.getSubjectName()
					+ " in examination " + examination.getId());
			eventService.publishEvent(event);

			// Manage archive
			if (importJob.getArchive() != null) {
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
			}

			// Send success mail
			mailService.sendImportEmail(importJob, userId, examination, generatedAcquisitions);

		} catch (Exception e) {
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("Unexpected error during the import: " + e.getMessage() + ", please contact an administrator.");
			event.setProgress(1f);
			eventService.publishEvent(event);
			LOG.error("Error during import for exam: {} : {}", importJob.getExaminationId(), e);
			
			// Send mail
			mailService.sendFailureMail(importJob, userId, e.getMessage());
			
			throw new ShanoirException(event.getMessage(), e);
		}
	}
	
	/**
	 *  Persist Dicom images in the Shanoir Pacs
	 * @throws Exception 
	 */
	private void persistSerieInPacs(Serie serie) throws Exception {
	    long startTime = System.currentTimeMillis();
        dicomPersisterService.persistAllForSerie(serie);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        LOG.info("Import of " + serie.getImagesNumber() + " DICOM images into the PACS required "
                + duration + " millis for serie: " + serie.getSeriesInstanceUID()
                + "(" + serie.getSeriesDescription() + ")");
    }

	/**
	 * Save DatasetAcquisition object in the Shanoir db
	 */
    private void persistAcquisition(DatasetAcquisition acquisition) {
        datasetAcquisitionService.create(acquisition);
    }

    public DatasetAcquisition createDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination, ImportJob importJob, StudyCard studyCard) throws Exception {
		if (checkSerieForDicomImages(serie)) {
		    
		    // get dicomAttributes
		    Attributes dicomAttributes = null;
	        try {
	            dicomAttributes = dicomProcessing.getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie(), serie.getIsEnhanced());
	        } catch (IOException e) {
	            throw new ShanoirException("Unable to retrieve dicom attributes in file " + serie.getFirstDatasetFileForCurrentSerie().getPath(), e);
	        }
		    
	        // generate acquisition
			DatasetAcquisition datasetAcquisition = datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, rank, importJob, dicomAttributes);			
			datasetAcquisition.setExamination(examination);
			// TODO: put studyCard in bruker import
			if (datasetAcquisition.getAcquisitionEquipmentId() == null) {
				datasetAcquisition.setAcquisitionEquipmentId(importJob.getAcquisitionEquipmentId());
			}
			
			// apply study card if needed
			if (studyCard != null) {
			    studyCard.apply(datasetAcquisition, null);
			}

			return datasetAcquisition;
		} else {
			LOG.warn("Serie " + serie.getSequenceName() + ", " + serie.getProtocolName() + " found without images. Ignored.");
		}
		return null;
	}
	
	private StudyCard getStudyCard(Long studyCardId) {
        StudyCard studyCard = studyCardRepository.findById(studyCardId).orElse(null);
        if (studyCard == null) {
            throw new IllegalArgumentException("No study card found with id " + studyCardId);
        }
        if (studyCard.getAcquisitionEquipmentId() == null) {
            throw new IllegalArgumentException("No acq eq id found for the study card " + studyCardId);
        }
        return studyCard;
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
	 * Create a processed dataset dataset associated with a dataset processing.
	 * @param importJob the import job from importer MS.
	 */
	public Dataset createProcessedDataset(final ProcessedDatasetImportJob importJob) throws IOException {

		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getProcessedDatasetFilePath(), KeycloakUtil.getTokenUserId(), "Starting import...", ShanoirEvent.IN_PROGRESS, 0f);
		eventService.publishEvent(event);

		if (importJob == null || importJob.getDatasetProcessing() == null) {
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("Dataset processing missing.");
			event.setProgress(1f);
			eventService.publishEvent(event);
			return null;
		}
		
		// Metadata
		DatasetMetadata originMetadata = new DatasetMetadata();
		originMetadata.setProcessedDatasetType(importJob.getProcessedDatasetType());
		originMetadata.setName(importJob.getProcessedDatasetName());

		try {
			DatasetProcessing datasetProcessing = importJob.getDatasetProcessing();
			Dataset dataset = null;
			
			switch(importJob.getDatasetType()) {
				case CalibrationDataset.datasetType:
					dataset = new CalibrationDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
					break;
				case CtDataset.datasetType:
					dataset = new CtDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.CT_DATASET);
					break;
				case EegDataset.datasetType:
					dataset = new EegDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.EEG_DATASET);
					break;
				case MegDataset.datasetType:
					dataset = new MegDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.EEG_DATASET);
					break;
				case MeshDataset.datasetType:
					dataset = new MeshDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
					break;
				case MrDataset.datasetType:
					dataset = new MrDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.MR_DATASET);
					break;
				case ParameterQuantificationDataset.datasetType:
					dataset = new ParameterQuantificationDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
					break;
				case PetDataset.datasetType:
					dataset = new PetDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.PET_DATASET);
					break;
				case RegistrationDataset.datasetType:
					dataset = new RegistrationDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
					break;
				case SegmentationDataset.datasetType:
					dataset = new SegmentationDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
					break;
				case SpectDataset.datasetType:
					dataset = new SpectDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.SPECT_DATASET);
					break;
				case StatisticalDataset.datasetType:
					dataset = new StatisticalDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
					break;
				case TemplateDataset.datasetType:
					dataset = new TemplateDataset();
					originMetadata.setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);
					break;
				default:
				break;
			}
			
			datasetProcessing.addOutputDataset(dataset);
			dataset.setDatasetProcessing(datasetProcessing);
			dataset.setStudyId(importJob.getStudyId());

			// Copy the data somewhere else
			final String subLabel = SUBJECT_PREFIX + importJob.getSubjectName();

			final File outDir = new File(niftiStorageDir + File.separator + PROCESSED_DATASET_PREFIX + File.separator + subLabel + File.separator);
			outDir.mkdirs();
			String filePath = importJob.getProcessedDatasetFilePath();
			File srcFile = new File(filePath);
			String originalNiftiName = srcFile.getName();
			File destFile = new File(outDir.getAbsolutePath() + File.separator + originalNiftiName);

			// Save file
			Path location = null;
			try {
				destFile.getParentFile().mkdirs();
				location = Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOG.error("IOException generating Processed Dataset Expression", e);
			}
			DatasetFile datasetFile = new DatasetFile();
			datasetFile.setPacs(false);
			datasetFile.setPath(location.toUri().toString());

			DatasetExpression expression = new DatasetExpression();
			expression.setDataset(dataset);
			expression.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
			expression.setDatasetProcessingType(datasetProcessing.getDatasetProcessingType());
			expression.setSize(Files.size(location));
			
			datasetFile.setDatasetExpression(expression);
			
			expression.setDatasetFiles(Collections.singletonList(datasetFile));
			
			dataset.setDatasetExpressions(Collections.singletonList(expression));

			// Fill dataset with informations
			dataset.setCreationDate(LocalDate.now());
			dataset.setOriginMetadata(originMetadata);
			dataset.setUpdatedMetadata(dataset.getOriginMetadata());
			dataset.setStudyId(importJob.getStudyId());
			dataset.setSubjectId(importJob.getSubjectId());

			dataset = datasetService.create(dataset);
			
			solrService.indexDataset(dataset.getId());

			event.setStatus(ShanoirEvent.SUCCESS);
			event.setMessage(importJob.getStudyName() + "(" + importJob.getStudyId() + ")"
					+": Successfully created processed dataset for subject " + importJob.getSubjectName() + " in dataset "
					+ dataset.getId());
			event.setProgress(1f);
			eventService.publishEvent(event);
			
			return dataset;
			
		} catch (Exception e) {
			LOG.error("Error while importing processed dataset: ", e);
			event.setStatus(ShanoirEvent.ERROR);
			event.setMessage("Unexpected error during the import of the processed dataset: " + e.getMessage() + ", please contact an administrator.");
			event.setProgress(1f);
			eventService.publishEvent(event);
			throw e;
		}
	}
	
}