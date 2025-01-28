package org.shanoir.ng.importer.service;

import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetDTO;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.model.*;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.eeg.EegDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class EegImporterService {

    private static final Logger LOG = LoggerFactory.getLogger(EegImporterService.class);

    @Value("${datasets-data}")
    private String niftiStorageDir;

    @Autowired
    private ImporterMailService mailService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private DatasetAcquisitionService datasetAcquisitionService;

    @Autowired
    private ShanoirEventService eventService;

    private static final String SESSION_PREFIX = "ses-";

    private static final String SUBJECT_PREFIX = "sub-";

    private static final String EEG_PREFIX = "eeg";

    /**
     * Create a dataset acquisition, and associated dataset.
     * @param importJob the import job from importer MS.
     */
    public void createEegDataset(final EegImportJob importJob) throws IOException {

        Long userId = KeycloakUtil.getTokenUserId();
        ShanoirEvent event;
        if(Objects.isNull(importJob.getShanoirEvent())){
            event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT, importJob.getExaminationId().toString(), userId, "Starting import...", ShanoirEvent.IN_PROGRESS, 0f, importJob.getStudyId());
        } else {
            event = importJob.getShanoirEvent();
        }
        eventService.publishEvent(event);

        if (importJob == null || importJob.getDatasets() == null || importJob.getDatasets().isEmpty()) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("No datasets to create. Please check your EEG files");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            return;
        }

        try {
            DatasetAcquisition datasetAcquisition = new EegDatasetAcquisition();

            // Get examination
            Examination examination = examinationService.findById(importJob.getExaminationId());

            datasetAcquisition.setExamination(examination);
            datasetAcquisition.setAcquisitionEquipmentId(importJob.getAcquisitionEquipmentId());
            datasetAcquisition.setRank(0);
            datasetAcquisition.setSortingIndex(0);
            datasetAcquisition.setUsername(importJob.getUsername());
            datasetAcquisition.setImportDate(LocalDate.now());

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

                long filesSize = 0L;

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
                            filesSize += Files.size(finalLocation);
                            file.setPacs(false);
                            files.add(file);
                        }
                    }
                }

                expression.setDatasetFiles(files);
                expression.setSize(filesSize);
                datasetToCreate.setDatasetExpressions(Collections.singletonList(expression));

                // set the dataset_id where needed
                for (Channel chan : datasetDto.getChannels()) {
                    chan.setDataset(datasetToCreate);
                    chan.setReferenceType(Channel.ChannelType.EEG);
                    // Parse channel name to get its type
                    for (Channel.ChannelType type : Channel.ChannelType.values()) {
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
                datasetToCreate.setUpdatedMetadata(originMetadata);
                datasetToCreate.setSubjectId(importJob.getSubjectId());
                datasetToCreate.setSamplingFrequency(datasetDto.getSamplingFrequency());
                datasetToCreate.setCoordinatesSystem(datasetDto.getCoordinatesSystem());

                datasets.add(datasetToCreate);
            }

            datasetAcquisition.setDatasets(datasets);
            datasetAcquisitionService.create(datasetAcquisition);

            event.setProgress(1f);
            event.setStatus(ShanoirEvent.SUCCESS);
            // This message is important for email service
            event.setMessage("[" + importJob.getStudyName() + " (n°" + importJob.getStudyId() + ")]"
                    +" Successfully created datasets for subject [" + importJob.getSubjectName()
                    + "] in examination [" + examination.getId() + "]");
            eventService.publishEvent(event);

            // Send mail
            mailService.sendImportEmail(importJob, userId, examination, Collections.singleton(datasetAcquisition));
        } catch (Exception e) {
            LOG.error("Error while importing EEG: ", e);
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("An unexpected error occured, please contact an administrator.");
            event.setProgress(-1f);
            eventService.publishEvent(event);

            // Send failure mail
            mailService.sendFailureMail(importJob, userId, e.getMessage());
            throw e;
        }
    }

}
