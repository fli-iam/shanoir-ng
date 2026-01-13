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
package org.shanoir.ng.dataset.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections4.ListUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetDownloadData;
import org.shanoir.ng.dataset.dto.DatasetLight;
import org.shanoir.ng.dataset.dto.DatasetStudyCenter;
import org.shanoir.ng.dataset.dto.VolumeByFormatDTO;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.datasetfile.DatasetFileRepository;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.study.rights.UserRights;
import org.shanoir.ng.studycard.dto.DicomTag;
import org.shanoir.ng.studycard.service.StudyCardService;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.vip.processingResource.repository.ProcessingResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dataset service implementation.
 *
 * @author msimon
 *
 */
@Service
public class DatasetServiceImpl implements DatasetService {

    @Autowired
    private DatasetRepository repository;

    @Autowired
    private StudyUserRightsRepository rightsRepository;

    @Autowired
    private StudyRightsService studyRightService;

    @Autowired
    private ShanoirEventService shanoirEventService;

    @Value("${dcm4chee-arc.dicom.web}")
    private boolean dicomWeb;

    @Autowired
    private DatasetPropertyService propertyService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private DatasetProcessingService processingService;

    @Autowired
    private DatasetTransactionalServiceImpl datasetTransactionalService;

    @Autowired
    private DatasetAsyncService datasetAsyncService;

    @Autowired
    private DatasetFileRepository datasetFileRepository;

    @Autowired
    private ProcessingResourceRepository processingResourceRepository;

    @Autowired
    private WADODownloaderService downloader;

    @Autowired
    private StudyCardService studyCardService;

    @Autowired
    @Lazy
    private SubjectRepository subjectRepository;

    private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

    private void delete(Dataset entity) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        Long id = entity.getId();

        // Remove parent processing to avoid errors
        entity.setDatasetProcessing(null);
        processingService.removeDatasetFromAllProcessingInput(id);
        processingResourceRepository.deleteByDatasetId(id);
        propertyService.deleteByDatasetId(id);

        List<Long> expressionIds = entity.getDatasetExpressions().stream()
                .map(DatasetExpression::getId)
                .filter(Objects::nonNull)
                .toList();

        if (!expressionIds.isEmpty()) {
            datasetFileRepository.deleteByDatasetExpressionIds(expressionIds);
        }
        repository.delete(entity);
    }

    /**
     * Call by dataset-details. Also reject from pacs
     * @param id dataset id.
     * @throws ShanoirException
     * @throws SolrServerException
     * @throws IOException
     * @throws RestServiceException
     */
    @Override
    @Transactional
    public void deleteById(final Long id) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        final Dataset dataset = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Dataset.class, id));
        // Do not delete entity if it is the source (or if it has copies). If getSourceId() is not null, it means it's a copy
        if (!CollectionUtils.isEmpty(dataset.getCopies())) {
            throw new RestServiceException(
                    new ErrorModel(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "This dataset is linked to another dataset that was copied."
                    ));
        }
        long startTime = System.currentTimeMillis();
        delete(dataset);
        deleteDatasetFilesFromDiskAndPacs(dataset);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        LOG.info("Dataset deletion time: " + elapsedTime + " milliseconds");
    }

    /**
     * Called by acquisition delete. Does not reject from pacs as acquisition already does it.
     * @param id
     * @throws ShanoirException
     * @throws SolrServerException
     * @throws IOException
     * @throws RestServiceException
     */
    public void deleteByIdCascade(final Long id) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        final Dataset dataset = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Dataset.class, id));

        // Do not delete entity if it is the source (or if it has copies). If getSourceId() is not null, it means it's a copy
        if (!CollectionUtils.isEmpty(dataset.getCopies())) {
            throw new RestServiceException(
                    new ErrorModel(
                            HttpStatus.UNPROCESSABLE_ENTITY.value(),
                            "This dataset is linked to another dataset that was copied."
                    ));
        }

        delete(dataset);
    }

    public void deleteDatasetFilesFromDiskAndPacs(Dataset dataset) throws ShanoirException {
        if (!dicomWeb) {
            return;
        }
        Long id = dataset.getId();
        for (DatasetExpression expression : dataset.getDatasetExpressions()) {
            boolean isDicom = DatasetExpressionFormat.DICOM.equals(expression.getDatasetExpressionFormat());
            List<DatasetFile> datasetFiles = expression.getDatasetFiles();
            if (dataset.getSource() == null)
                datasetAsyncService.deleteDatasetFilesFromDiskAndPacsAsync(datasetFiles, isDicom, id);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteByIdIn(List<Long> ids) throws ShanoirException, SolrServerException, IOException, RestServiceException {
        for (Long id : ids) {
            this.deleteById(id);
        }
    }

    @Override
    public Dataset findById(final Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public int countByStudyId(Long studyId) {
        return repository.countByDatasetAcquisition_Examination_Study_Id(studyId);
    }

    @Override
    public List<Dataset> findByIdIn(List<Long> ids) {
        return Utils.toList(repository.findAllById(ids));
    }

    @Override
    public List<DatasetLight> findLightByIdIn(List<Long> ids) {
        return Utils.toList(repository.findAllLightById(ids));
    }

    @Override
    public List<DatasetLight> findLightByStudyId(Long studyId) {
        return Utils.toList(repository.findAllLightByStudyId(studyId));
    }

    @Override
    public Dataset create(final Dataset dataset) throws SolrServerException, IOException {
        Dataset ds = repository.save(dataset);
        Long studyId;
        if (ds.getDatasetAcquisition() != null) {
            studyId = ds.getDatasetAcquisition().getExamination().getStudyId();
        } else {
            // We have a processed dataset -> acquisition is null but study id is set.
            studyId = ds.getStudyId();
        }

        shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, ds.getStudyId()));
        rabbitTemplate.convertAndSend(RabbitMQConfiguration.RELOAD_BIDS, objectMapper.writeValueAsString(studyId));
        return ds;
    }

    @Override
    public Dataset update(final Dataset dataset) throws EntityNotFoundException {
        final Dataset datasetDb = repository.findById(dataset.getId()).orElse(null);
        if (datasetDb == null) {
            throw new EntityNotFoundException(Dataset.class, dataset.getId());
        }
        this.updateDatasetValues(datasetDb, dataset);
        Dataset ds = repository.save(datasetDb);
        try {
            Long studyId;
            if (ds.getDatasetProcessing() == null) {
                studyId = ds.getDatasetAcquisition().getExamination().getStudyId();
            } else {
                studyId = ds.getStudyId();
            }
            shanoirEventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_DATASET_EVENT, ds.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, studyId));
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.RELOAD_BIDS, objectMapper.writeValueAsString(studyId));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while updating a dataset", e);
        }
        return ds;
    }


    /**
     * Update some values of dataset to save them in database.
     *
     * @param datasetDb dataset found in database.
     * @param dataset dataset with new values.
     * @return database dataset with new values.
     */
    private Dataset updateDatasetValues(final Dataset datasetDb, final Dataset dataset) {
        datasetDb.setCreationDate(dataset.getCreationDate());
        datasetDb.setId(dataset.getId());
        datasetDb.setProcessings(dataset.getProcessings());
        datasetDb.setSubjectId(dataset.getSubjectId());
        if (dataset.getUpdatedMetadata().getId().equals(dataset.getOriginMetadata().getId())) {
            // Force creation of a new dataset metadata
            dataset.getUpdatedMetadata().setId(null);
        }
        datasetDb.setUpdatedMetadata(dataset.getUpdatedMetadata());
        if (dataset instanceof MrDataset) {
            MrDataset mrDataset = (MrDataset) dataset;
            ((MrDataset) datasetDb).setUpdatedMrMetadata(mrDataset.getUpdatedMrMetadata());
        }
        return datasetDb;
    }

    @Override
    public Page<Dataset> findPage(final Pageable pageable) {

        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return repository.findAll(pageable);
        }

        Long userId = KeycloakUtil.getTokenUserId();
        List<Long> studyIds = rightsRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());

        // Check if user has restrictions.
        boolean hasRestrictions = false;
        List<StudyUser> studyUsers = Utils.toList(rightsRepository.findByUserId(userId));
        Map<Long, List<Long>> studyUserCenters = new HashMap<>();
        for (StudyUser studyUser : studyUsers) {
            if (!CollectionUtils.isEmpty(studyUser.getCenterIds())) {
                hasRestrictions = true;
                studyUserCenters.put(studyUser.getStudyId(), studyUser.getCenterIds());
            }
        }

        if (!hasRestrictions) {
            return repository.findByDatasetAcquisitionExaminationStudy_IdIn(studyIds, pageable);
        }

        // If yes, get all examinations and filter by centers
        List<Dataset> datasets = Utils.toList(repository.findByDatasetAcquisitionExaminationStudy_IdIn(studyIds, pageable.getSort()));

        if (CollectionUtils.isEmpty(datasets)) {
            return new PageImpl<>(datasets);
        }

        datasets = datasets.stream().filter(ds ->
                        studyUserCenters.get(ds.getStudyId()) != null
                                && studyUserCenters.get(ds.getStudyId()).contains(ds.getDatasetAcquisition().getExamination().getCenterId()))
                .collect(Collectors.toList());
        int size = datasets.size();

        datasets = datasets.subList(pageable.getPageSize() * pageable.getPageNumber(), Math.min(datasets.size(), pageable.getPageSize() * (pageable.getPageNumber() + 1)));

        Page<Dataset> page = new PageImpl<>(datasets, pageable, size);
        return page;
    }

    @Override
    public List<Dataset> findByStudyId(Long studyId) {
        return Utils.toList(repository.findByDatasetAcquisition_Examination_Study_Id(studyId));
    }

    @Override
    public List<VolumeByFormatDTO> getVolumeByFormat(Long studyId) {
        List<Object[]> results = repository.findExpressionSizesByStudyIdGroupByFormat(studyId);
        List<VolumeByFormatDTO> sizesByFormat = new ArrayList<>();

        for (Object[] result : results) {
            sizesByFormat.add(new VolumeByFormatDTO(DatasetExpressionFormat.getFormat((int) result[0]), (Long) result[1]));
        }

        return sizesByFormat;

    }

    @Override
    public Map<Long, List<VolumeByFormatDTO>> getVolumeByFormatByStudyId(List<Long> studyIds) {
        List<Object[]> results = repository.findExpressionSizesTotalByStudyIdGroupByFormat(studyIds);
        Map<Long, List<VolumeByFormatDTO>> sizesByFormatAndStudy = new HashMap<>();

        for (Long id : studyIds) {
            sizesByFormatAndStudy.putIfAbsent(id, new ArrayList<>());
        }

        for (Object[] result : results) {
            Long studyId = (Long) result[0];
            sizesByFormatAndStudy.get(studyId).add(new VolumeByFormatDTO(DatasetExpressionFormat.getFormat((int) result[1]), (Long) result[2]));
        }

        return sizesByFormatAndStudy;

    }

    @Override
    public List<Dataset> findByAcquisition(Long acquisitionId) {
        return Utils.toList(repository.findByDatasetAcquisitionId(acquisitionId));
    }

    @Override
    public List<Dataset> findByStudycard(Long studycardId) {
        if (KeycloakUtil.getTokenRoles().contains("ROLE_ADMIN")) {
            return Utils.toList(repository.findBydatasetAcquisitionStudyCardId(studycardId));
        } else {
            Long userId = KeycloakUtil.getTokenUserId();
            List<Long> studyIds = rightsRepository.findDistinctStudyIdByUserId(userId, StudyUserRight.CAN_SEE_ALL.getId());

            return Utils.toList(repository.findByDatasetAcquisitionStudyCardIdAndDatasetAcquisitionExaminationStudy_IdIn(studycardId, studyIds));
        }
    }

    @Override
    public List<Dataset> findByExaminationId(Long examinationId) {
        return Utils.toList(repository.findByDatasetAcquisitionExaminationId(examinationId));
    }

    @Override
    public List<Dataset> findDatasetAndOutputByExaminationId(Long examinationId) {
        return StreamSupport.stream(repository.findAllById(repository.findDatasetAndOutputByExaminationId(examinationId)).spliterator(), false).toList();
    }

    @Async
    public void deleteNiftis(Long studyId) {
        List<Long> datasets = repository.findIdsByStudyId(studyId);

        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DELETE_NIFTI_EVENT, studyId.toString(), KeycloakUtil.getTokenUserId(), "Preparing deletion of niftis", ShanoirEvent.IN_PROGRESS, 0, studyId);
        shanoirEventService.publishEvent(event);
        try {
            int total = datasets.size();
            updateEvent(0f, event, studyId);
            for (List<Long> partition : ListUtils.partition(datasets, 1000)) {
                datasetTransactionalService.deletePartitionOfNiftis(partition, total, event).get();
            }
            updateEvent(1f, event, studyId);
        } catch (Exception e) {
            updateEvent(-1f, event, studyId, e);
        }
    }

    protected void updateEvent(float progress, ShanoirEvent event) {
        updateEvent(progress, event, null, null);
    }

    protected void updateEvent(float progress, ShanoirEvent event, Long id) {
        updateEvent(progress, event, id, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateEvent(float progress, ShanoirEvent event, Long id, Exception e) {
        event.setProgress(progress);
        if (progress == 1f) {
            event.setStatus(ShanoirEvent.SUCCESS);
            event.setMessage("Deleting nifti for study: " + id + ": Success.");
        } else if (progress == -1f) {
            LOG.error("Could not properly delete niftis: ", e);
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Deleting nifti for study: " + id + ": Error. " + e.getMessage());
        } else if (Objects.nonNull(id)) {
            event.setMessage("Deleting nifti for study: " + id);
        }
        shanoirEventService.publishEvent(event);
    }

    /**
     * Get study Id from dataset. If processed, recursively get it through processing inputs
     *
     * @param dataset
     * @return
     */
    @Override
    public Long getStudyId(Dataset dataset) {
        if (dataset.getStudyId() != null) {
            return dataset.getStudyId();
        }
        if (dataset.getDatasetProcessing() != null) {
            return dataset.getDatasetProcessing().getStudyId();
        }
        if (dataset.getDatasetAcquisition() != null && dataset.getDatasetAcquisition().getExamination() != null) {
            return dataset.getDatasetAcquisition().getExamination().getStudyId();
        }
        try {
            LOG.error(objectMapper.writeValueAsString(dataset));
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get examination from dataset. If processed, recursively get it through processing inputs
     *
     * @param dataset
     * @return
     */
    @Override
    public Examination getExamination(Dataset dataset) {
        DatasetAcquisition acquisition = this.getAcquisition(dataset);
        if (acquisition != null) {
            return acquisition.getExamination();
        }
        return null;
    }

    @Override
    public DatasetAcquisition getAcquisition(Dataset dataset) {
        if (dataset.getDatasetAcquisition() != null) {
            return dataset.getDatasetAcquisition();
        }
        if (dataset.getDatasetProcessing().getInputDatasets() != null) {
            for (Dataset ds : dataset.getDatasetProcessing().getInputDatasets()) {
                DatasetAcquisition acq = this.getAcquisition(ds);
                if (acq != null) {
                    return acq;
                }
            }
        }
        return null;
    }

    @Override
    public List<DatasetDownloadData> getDownloadDataByAcquisitionAndExaminationIds(List<Long> acquisitionIds,
            List<Long> examinationIds) {

        UserRights userRights = studyRightService.getUserRights();
        Set<DatasetStudyCenter> dsList = repository.getDatasetsByAcquisitionAndExaminationIds(acquisitionIds, examinationIds);
        List<DatasetDownloadData> downloadDataList = new ArrayList<>();
        for (DatasetStudyCenter dsData : dsList) {
            DatasetDownloadData downloadData = new DatasetDownloadData();
            downloadData.setId(dsData.getDatasetId());
            boolean canDownload =
                    userRights.hasStudyRights(dsData.getStudyId(), StudyUserRight.CAN_DOWNLOAD)
                    && (!userRights.hasCenterRestrictionsFor(dsData.getStudyId())
                        || userRights.hasStudyCenterRights(dsData.getStudyId(), dsData.getCenterId()));
            downloadData.setCanDownload(canDownload);
            downloadDataList.add(downloadData);
        }
        return downloadDataList;
    }

    public File extractDicomMetadata(List<Long> datasetIds, List<String> metadataKeys) throws Exception {
        LOG.info("Metdata file download started.");
        File metadataFile = createMetadataFile(metadataKeys);
        fillMetadataFile(metadataFile, datasetIds, metadataKeys);
        return metadataFile;
    }

    protected void fillMetadataFile(File metadataFile, List<Long> datasetIds, List<String> metadataKeys) throws Exception {
        LOG.info("Filling metadata file.");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataFile, true))) {
            ObjectMapper mapper = new ObjectMapper();

            for (Long datasetId : datasetIds) {
                String metadataLine = shapeMetadataLinesForOneDicom(metadataKeys, datasetId, mapper);
                writer.newLine();
                writer.write(metadataLine);
            }
        } catch (Exception e) {
            LOG.error("An error occured while writing a metadata line");
            throw e;
        }
    }

    protected String shapeMetadataLinesForOneDicom(List<String> metadataKeys, Long datasetId, ObjectMapper mapper) {
        String metadataLine = datasetId.toString();
        final Dataset dataset = findById(datasetId);
        metadataLine += ";" + (Objects.nonNull(dataset.getDatasetAcquisition()) ? dataset.getDatasetAcquisition().getId().toString() : "");
        JsonNode metadatas = null;
        try {
            String metadataStr = getMetadataFromDicom(dataset);
            metadatas = mapper.readTree(metadataStr).get(0);
        } catch (Exception ignored) {
        }

        for (String metadataKey : metadataKeys) {
            metadataLine += ";";
            if (Objects.nonNull(metadatas) && metadatas.has(metadataKey) && metadatas.get(metadataKey).has("Value")) {
                metadataLine += metadatas.get(metadataKey).get("Value").get(0).asText();
            }
        }
        return metadataLine;
    }

    protected String getMetadataFromDicom(Dataset dataset) throws Exception {
        DatasetDownloadError result = new DatasetDownloadError();
        List<URL> pathURLs = new ArrayList<>();
        DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM, result);

        if (pathURLs.isEmpty()) {
            LOG.error("No metadata found for dataset " + dataset.getId());
            throw new Exception("No metadata found for dataset " + dataset.getId());
        } else {
            try {
                return downloader.downloadDicomMetadataForURL(pathURLs.get(0));
            } catch (Exception e) {
                LOG.error("Loading metadata for dataset " + dataset.getId() + " failed");
                throw e;
            }
        }
    }

    protected File createMetadataFile(List<String> metadataKeys) throws Exception {
        File metadataFile = new File("/tmp/metadataExtraction.csv");
        try {
            if (!metadataFile.createNewFile()) {
                metadataFile.delete();
                metadataFile.createNewFile();
            }

            List<DicomTag> dicomTags = studyCardService.findDicomTags();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataFile))) {
                String headers = "Dataset_Id;Acquisition_Id;" + metadataKeys.stream().map(key -> dicomTags.stream().filter(tag -> Objects.equals(Long.parseLong(key, 16), (long) tag.getCode())).findAny().map(tag -> tag.getLabel() + " " + key).orElse("Unknown " + key)).collect(Collectors.joining(";"));
                writer.write(headers);
                LOG.info("Headers for metadata file : " + headers);
            } catch (IOException e) {
                throw new Exception("An error occured while creating metadata extraction file", e);
            }
        } catch (Exception e) {
            LOG.error("An error occured while creating metadata extraction file");
            throw e;
        }
        LOG.info("Metadata file created");
        return metadataFile;
    }
}
