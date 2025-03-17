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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.awaitility.reflect.exception.FieldNotFoundException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.dataset.service.DatasetUtils;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.strategies.datasetexpression.DicomDatasetExpressionStrategy;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.service.SubjectStudyService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.QualityException;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
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

@Service
@Scope("prototype")
public class ImporterService {

    private static final Logger LOG = LoggerFactory.getLogger(ImporterService.class);

    private static final String PROCESSED_DATASET_PREFIX = "processed-dataset";

    private static final String UPLOAD_EXTENSION = ".upload";

    private static final String SUBJECT_PREFIX = "sub-";    

    private static int instancesCreated = 0;

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
    private DicomPersisterService dicomPersisterService;

    @Autowired
    private DICOMWebService dicomWebService;

    @Autowired
    private DicomDatasetExpressionStrategy dicomDatasetExpressionStrategy;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private ImporterMailService mailService;

    @Autowired
    private StudyCardRepository studyCardRepository;
   
    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Autowired
	private SubjectStudyService subjectStudyService;

    @Autowired
    private SolrService solrService;

    @Autowired
    private QualityService qualityService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    //This constructor will be called everytime a new bean instance is created
    public ImporterService() {
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
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        Set<DatasetAcquisition> generatedAcquisitions = null;
        try {
            Examination examination = examinationRepository.findById(importJob.getExaminationId()).orElse(null);
            if (examination != null) {
                // generate acquisitions
                generatedAcquisitions = generateAcquisitions(examination, importJob, event);
                examination.getDatasetAcquisitions().addAll(generatedAcquisitions); // change to set() ?
                // Quality check
                SubjectStudy subjectStudy = examination.getSubject().getSubjectStudyList().stream()
                    .filter(ss -> ss.getStudy().getId().equals(examination.getStudy().getId()))
                    .findFirst().orElse(null);
                QualityTag tagSave = subjectStudy != null ? subjectStudy.getQualityTag() : null;
                ExaminationData examData = new ExaminationData(examination);
                examData.setDatasetAcquisitions(Utils.toList(generatedAcquisitions));
                QualityCardResult qualityResult;
                
                // If import comes from ShanoirUploader, the check quality at import has already been done
                if (!importJob.isFromShanoirUploader()) {
                    qualityResult = qualityService.checkQuality(examData, importJob, null);
                } else {
                    LOG.info("Importing Data from ShanoirUploader.");
                    // We retrieve quality card result from ShUp import job
                    qualityResult = qualityService.retrieveQualityCardResult(importJob);
                    if (!qualityResult.isEmpty()) {
                        LOG.info("Retrieving Quality Control result from ShanoirUploader.");
                        if(subjectStudy != null) {
                            subjectStudy.setQualityTag(qualityResult.get(0).getTagSet());
                            qualityResult.addUpdatedSubjectStudy(subjectStudy);
                        }
                    }
                }
                                				
                // Has quality check passed ?
                if (qualityResult != null && !qualityResult.isEmpty() && qualityResult.hasError()) {
                    throw new QualityException(examination, qualityResult);
                } else { // Then do the import
                    if (qualityResult != null && !qualityResult.isEmpty()) {
                        if (qualityResult.hasWarning() || qualityResult.hasFailedValid()) {
                            event.setReport(qualityResult.toString());
                        }
                        // add tag to subject-study
                        subjectStudyService.update(qualityResult.getUpdatedSubjectStudies());
                    }
                	generatedAcquisitions = new HashSet<>(datasetAcquisitionService.createAll(generatedAcquisitions));
                    try {
                        persistPatientInPacs(importJob.getPatients(), event);
                    } catch (Exception e) { // if error in pacs
                        // revert dataset acquisitions
                        for (DatasetAcquisition acquisition : generatedAcquisitions) {
                            datasetAcquisitionService.deleteById(acquisition.getId(), null);
                        }
                        // revert quality tag
                        if(subjectStudy != null) {
                            subjectStudy.setQualityTag(tagSave);
                            subjectStudyService.update(qualityResult.getUpdatedSubjectStudies());
                        }
                        throw new ShanoirException("Error while saving data in pacs, the import is canceled and acquisitions were not saved", e);
                    }
                }
            } else {
                throw new ShanoirException("Examination not found: " + importJob.getExaminationId());
            }

            event.setProgress(1f);
            event.setStatus(ShanoirEvent.SUCCESS);

            event.setMessage("[" + importJob.getStudyName() + " (n°" + importJob.getStudyId() + ")]"
                    +" Successfully created datasets for subject [" + importJob.getSubjectName()
                    + "] in examination [" + examination.getId() + "]");
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

        } catch (QualityException e) {
            String msg = e.buildErrorMessage();
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Quality checks didn't pass at import, import aborted");
            event.setReport(e.getQualityResult().toString());
            event.setProgress(-1f);
            eventService.publishEvent(event);
            LOG.warn(msg, e);	
            // Send mail
            mailService.sendFailureMail(importJob, userId, msg);
            throw new ShanoirException(msg, e);
        } catch (Exception e) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Unexpected error during the import: " + e.getClass() + " : " + e.getMessage() + ", please contact an administrator.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            LOG.error("Error during import for exam: {} : {}", importJob.getExaminationId(), e); 
            // Send mail
            mailService.sendFailureMail(importJob, userId, e.getMessage());
            throw new ShanoirException(event.getMessage(), e);
        }
    }
    
    private Set<DatasetAcquisition> generateAcquisitions(Examination examination, ImportJob importJob, ShanoirEvent event) throws Exception {
        StudyCard studyCard = getStudyCard(importJob);
        Set<DatasetAcquisition> generatedAcquisitions = new HashSet<>();
        int rank = 0;
        for (Patient patient : importJob.getPatients()) {
            for (Study study : patient.getStudies()) {
                float progress = 0.5f;
                for (Serie serie : study.getSelectedSeries() ) {
                    // get dicomAttributes
                    AcquisitionAttributes<String> dicomAttributes = null;
                    try {
                        dicomAttributes = DicomProcessing.getDicomAcquisitionAttributes(serie, serie.getIsEnhanced());
                    } catch (PacsException e) {
                        throw new ShanoirException("Unable to retrieve dicom attributes in file " + serie.getFirstDatasetFileForCurrentSerie().getPath(), e);
                    }
                    
                    // Generate acquisition object with all sub objects : datasets, protocols, expressions, ...
                    DatasetAcquisition acquisition = createDatasetAcquisitionForSerie(serie, rank, examination, importJob, dicomAttributes);
                    
                    // apply study card if needed
                    if (studyCard != null) { 
                        importJob.setStudyCardName(studyCard.getName());
                        studyCard.apply(acquisition, dicomAttributes);
                    }
                    
                    // add acq to collection
                    if (acquisition != null) {
                        generatedAcquisitions.add(acquisition);
                    }
                    rank++;
                    progress += 0.25f / study.getSelectedSeries().size();
                    event.setMessage("Generating Shanoir data from serie " + serie.getSeriesDescription()+ " to examination " + importJob.getExaminationId());
                    event.setProgress(progress);
                    eventService.publishEvent(event);
                }
            }
        }
        return generatedAcquisitions;
    }

    StudyCard getStudyCard(ImportJob importJob) {
        if (importJob.getStudyCardId() != null) { // makes sense: imports without studycard exist
            StudyCard studyCard = getStudyCard(importJob.getStudyCardId());
            return studyCard;
        } else {
            LOG.warn("No studycard given for this import.");
            return null;
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
     *  Persist Dicom images in the Shanoir Pacs
     * @throws Exception 
     */
    private void persistPatientInPacs(List<Patient> patients, ShanoirEvent event) throws Exception {
        for (Patient patient : patients) {
            for (Study study : patient.getStudies()) {
                float progress = 0.75f;
                for (Serie serie : study.getSelectedSeries() ) {
                    if (serie.getSelected() != null && serie.getSelected()) {
                        persistSerieInPacs(serie);
                    }
                    progress += 0.25f / study.getSelectedSeries().size();
                    event.setMessage("Saving serie " + serie.getSeriesDescription()+ " into pacs");
                    event.setProgress(progress);
                    eventService.publishEvent(event);
                }
            }
        }           
    }

    public DatasetAcquisition createDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination, ImportJob importJob, AcquisitionAttributes<String> dicomAttributes) throws Exception {
        if (checkSerieForDicomImages(serie)) {
            DatasetAcquisition datasetAcquisition = datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, rank, importJob, dicomAttributes);			
            datasetAcquisition.setExamination(examination);
            if (datasetAcquisition.getAcquisitionEquipmentId() == null) {
                datasetAcquisition.setAcquisitionEquipmentId(importJob.getAcquisitionEquipmentId());
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
    private static boolean checkSerieForDicomImages(Serie serie) {
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
     * 
     * @param importJob
     */
    public Dataset createProcessedDataset(final ProcessedDatasetImportJob importJob) throws Exception {
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getProcessedDatasetFilePath(), KeycloakUtil.getTokenUserId(), "Starting import...", ShanoirEvent.IN_PROGRESS, 0f, importJob.getStudyId());
        eventService.publishEvent(event);

        DatasetProcessing datasetProcessing = importJob.getDatasetProcessing();
        if (!checkProcessedDatasetImportJob(importJob, event)) {
            return null;
        }

        try {
            Dataset dataset = createDataset(importJob);

            File processedDatasetFile = new File(importJob.getProcessedDatasetFilePath());
            Attributes attributes = checkIfDICOM(processedDatasetFile);

            DatasetExpression expression = new DatasetExpression();
            expression.setDataset(dataset);
            expression.setDatasetProcessingType(datasetProcessing.getDatasetProcessingType());
            expression.setSize(Files.size(processedDatasetFile.toPath()));
            dataset.setDatasetExpressions(Collections.singletonList(expression));

            DatasetFile datasetFile = new DatasetFile();
            datasetFile.setDatasetExpression(expression);
            // Processed dataset: DICOM
            if (attributes != null) {
                expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
                datasetFile.setPacs(true);
                datasetFile = dicomDatasetExpressionStrategy.setPath(attributes, datasetFile);
                dicomWebService.sendDicomFileToPacs(processedDatasetFile);
            // Processed dataset: other, e.g. NIfTI
            } else {
                expression.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
                datasetFile.setPacs(false);
                Path location = saveProcessedDatasetFile(importJob, processedDatasetFile);
                datasetFile.setPath(location.toUri().toString());
            }
            expression.setDatasetFiles(Collections.singletonList(datasetFile));
            
            dataset = datasetService.create(dataset);
            solrService.indexDataset(dataset.getId());

            event.setStatus(ShanoirEvent.SUCCESS);
            event.setMessage("[" + importJob.getStudyName() + " (n°" + importJob.getStudyId() + ")] " +
                    "Successfully created processed dataset [" + dataset.getId() + "] " +
                    "for subject [" + importJob.getSubjectName() + "]");
            event.setProgress(1f);
            eventService.publishEvent(event);
            return dataset;
        } catch (Exception e) {
            LOG.error("Error while importing processed dataset: ", e);
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Unexpected error during the import: " + e.getClass() + " : " + e.getMessage() + ", please contact an administrator.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            throw e;
        }
    }

    public void createFailedJob(String datasetFilePath){
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, datasetFilePath, KeycloakUtil.getTokenUserId(), "Import of dataset failed.", ShanoirEvent.ERROR, -1f);
        eventService.publishEvent(event);
    }

    private Attributes checkIfDICOM(File processedDatasetFile) throws FieldNotFoundException {
        if (processedDatasetFile.exists()) {
            // We pass here by using DicomInputStream,
            // in case processed dataset file does not end with .dcm
            try (DicomInputStream dIS = new DicomInputStream(processedDatasetFile)) {
                return dIS.readDataset(); // we close InputStream and use Attributes
            } catch (IOException e) {
                // We ignore the exception here: if not DICOM, we assume other format
            }
        } else {
            LOG.error("Processed dataset file not existing: {}", processedDatasetFile.getAbsolutePath());
            throw new FieldNotFoundException("Processed dataset file not existing.");
        }
        return null;
    }

    /**
     * Check ProcessedDatasetImportJob.
     * 
     * @param job
     * @param event
     * @return
     */
    private boolean checkProcessedDatasetImportJob(ProcessedDatasetImportJob job, ShanoirEvent event) {
        if (job.getDatasetProcessing() == null) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Dataset processing missing.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            return false;
        }
        if (job.getDatasetProcessing().getInputDatasets() == null ||
                job.getDatasetProcessing().getInputDatasets().isEmpty()) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Processing input dataset(s) missing.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            return false;
        }
        if (job.getStudyId() == null) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Study missing.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            return false;
        }
        for(Dataset input : job.getDatasetProcessing().getInputDatasets()){
            Long studyId = datasetService.getStudyId(input);
            if (studyId != null && !studyId.equals(job.getStudyId())) {
                event.setStatus(ShanoirEvent.ERROR);
                event.setMessage("Study from input dataset [" + input.getId() + "] not the same as [" + studyId + "]");
                event.setProgress(-1f);
                eventService.publishEvent(event);
                return false;
            }
        }
        return true;
    }

    private Dataset createDataset(ProcessedDatasetImportJob job) {
        Dataset dataset = DatasetUtils.buildDatasetFromType(job.getDatasetType());
        dataset.getOriginMetadata().setProcessedDatasetType(job.getProcessedDatasetType());
        dataset.getOriginMetadata().setName(job.getProcessedDatasetName());
        dataset.setStudyId(job.getStudyId());
        dataset.setSubjectId(job.getSubjectId());
        dataset.setCreationDate(LocalDate.now());
        dataset.setUpdatedMetadata(dataset.getOriginMetadata());
        dataset.setDatasetProcessing(job.getDatasetProcessing());
        job.getDatasetProcessing().addOutputDataset(dataset);
        return dataset;
    }

    private Path saveProcessedDatasetFile(ProcessedDatasetImportJob job, File processedDatasetFile) throws IOException {
        final String subLabel = SUBJECT_PREFIX + job.getSubjectName();
        final File outDir = new File(niftiStorageDir + File.separator + PROCESSED_DATASET_PREFIX + File.separator + subLabel + File.separator);
        outDir.mkdirs();
        String fileName = processedDatasetFile.getName();
        File destFile = new File(outDir.getAbsolutePath() + File.separator + formatter.format(LocalDateTime.now()) + File.separator + fileName);
        Path location;
        try {
            destFile.getParentFile().mkdirs();
            location = Files.copy(processedDatasetFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return location;
        } catch (IOException e) {
            LOG.error("IOException generating Processed Dataset Expression", e);
            throw e;
        }
    }
    
}