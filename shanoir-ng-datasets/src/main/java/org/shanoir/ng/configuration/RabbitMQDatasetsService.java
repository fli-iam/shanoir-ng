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

package org.shanoir.ng.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.bids.service.BIDSService;
import org.shanoir.ng.dataset.dto.StudyStorageVolumeDTO;
import org.shanoir.ng.dataset.model.CopyReport;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.CsvCopyService;
import org.shanoir.ng.dataset.service.DatasetCopyService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dataset.RelatedDataset;
import org.shanoir.ng.shared.dto.StudyExaminationsDTO;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.AcquisitionEquipment;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.model.SubjectBatchDTO;
import org.shanoir.ng.shared.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.tag.model.Tag;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RabbitMQ configuration.
 */
@Component
public class RabbitMQDatasetsService {

    private static final String RABBIT_MQ_ERROR = "Something went wrong deserializing the event.";

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private DatasetCopyService datasetCopyService;

    @Autowired
    private RabbitMqStudyUserService listener;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

    @Autowired
    private SolrService solrService;

    @Autowired
    private ExaminationService examinationService;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private StudyCardRepository studyCardRepository;

    @Autowired
    private QualityCardRepository qualityCardRepository;

    @Autowired
    private BIDSService bidsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudyService studyService;

    @Autowired
    private DatasetSecurityService securityService;

    @Autowired
    private CsvCopyService csvCopyService;

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQDatasetsService.class);

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_DATASET, durable = "true"),
            exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
            autoDelete = "false", durable = "true", type = ExchangeTypes.FANOUT)), containerFactory = "multipleConsumersFactory"
    )
    public void receiveMessage(String commandArrStr) {
        listener.receiveStudyUsers(commandArrStr);
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_UPDATE_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    public String receiveStudyUpdate(final String studyAsString) {
        try {

            Study updated = objectMapper.readValue(studyAsString, Study.class);
            bidsService.deleteBidsFolder(updated.getId(), null);
            Study current = this.receiveAndUpdateIdNameEntity(studyAsString, Study.class, studyRepository);
            List<String> errors = studyService.validate(updated, current);
            if (!errors.isEmpty()) {
                return errors.get(0);
            }
            studyService.updateStudy(updated, current);
            try {
                solrService.updateStudyAsync(current.getId());
            } catch (Exception e) {
                LOG.error("Solr update failed for study {}", current.getId(), e);
            }
        } catch (Exception ex) {
            LOG.error("An error occured while processing study update", ex);
            return ex.getMessage();
        }
        return "";
    }

    @RabbitListener(queues = RabbitMQConfiguration.SUBJECT_UPDATE_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    public boolean receiveSubjectUpdate(final String subjectStr) {
        try {
            manageSubjectUpdate(subjectStr);
            return true;
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR, e);
        }
    }

    /**
     * MK: to avoid endless loops rabbitmq re-sending the same message,
     * we separate the @Transactional and @RabbitListener in two methods,
     * the the Amqp exception correctly arrives back to rabbitmq.
     *
     * @param subjectStr
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Transactional
    private void manageSubjectUpdate(final String subjectStr) throws JsonProcessingException, JsonMappingException {
        Subject subject = objectMapper.readValue(subjectStr, Subject.class);
        subject = subjectRepository.save(subject);
        LOG.info("Subject replicated in MS Datasets with ID: {} and Name: {}",
                subject.getId(), subject.getName()
        );
        // Update BIDS
        Set<Long> studyIds = new HashSet<>();
        for (Examination exam : examinationRepository.findBySubjectId(subject.getId())) {
            studyIds.add(exam.getStudyId());
        }
        for (Study stud : studyRepository.findAllById(studyIds)) {
            bidsService.deleteBidsFolder(stud.getId(), stud.getName());
        }
        // Update solr references
        List<Long> subjectIdList = new ArrayList<Long>();
        subjectIdList.add(subject.getId());
        try {
            solrService.updateSubjectsAsync(subjectIdList);
        } catch (Exception e) {
            LOG.error("Solr update failed for subjects {}", subjectIdList, e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.SUBJECT_BATCH_UPDATE_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    public boolean receiveSubjectBatchUpdate(final String subjectBatchStr) {
        try {
            manageSubjectBatchUpdate(subjectBatchStr);
            return true;
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR, e);
        }
    }

    @Transactional
    private void manageSubjectBatchUpdate(final String subjectBatchStr) throws JsonProcessingException {
        SubjectBatchDTO batchDTO = objectMapper.readValue(subjectBatchStr, SubjectBatchDTO.class);
        Set<Long> allStudyIds = new HashSet<>();
        List<Long> allSubjectIds = new ArrayList<>();
        for (Subject subject : batchDTO.getSubjects()) {
            subject = subjectRepository.save(subject);
            allSubjectIds.add(subject.getId());
            LOG.info("Subject replicated in MS Datasets with ID: {} and Name: {}",
                    subject.getId(), subject.getName());
            for (Examination exam : examinationRepository.findBySubjectId(subject.getId())) {
                allStudyIds.add(exam.getStudyId());
            }
        }
        // Update BIDS for all affected studies
        for (Study stud : studyRepository.findAllById(allStudyIds)) {
            bidsService.deleteBidsFolder(stud.getId(), stud.getName());
        }
        // Update Solr references in batch
        try {
            solrService.updateSubjectsAsync(allSubjectIds);
        } catch (Exception e) {
            LOG.error("Solr update failed for subjects {}", allSubjectIds, e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.ACQUISITION_EQUIPMENT_UPDATE_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    public void receiveAcEqUpdate(final String acEqStr) {
        receiveAndUpdateIdNameEntity(acEqStr, AcquisitionEquipment.class, acquisitionEquipmentRepository);
    }

    @RabbitListener(queues = RabbitMQConfiguration.CENTER_UPDATE_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    public void receiveCenterUpdate(final String centerStr) throws JsonMappingException, JsonProcessingException {
        Center center = objectMapper.readValue(centerStr, Center.class);
        saveCenter(center);
    }

    @Transactional
    private void saveCenter(Center center) {
        centerRepository.save(center);
    }

    @RabbitListener(queues = RabbitMQConfiguration.CENTER_DELETE_QUEUE, containerFactory = "singleConsumerFactory")
    @RabbitHandler
    public void receiveCenterDelete(final Long centerId) throws JsonMappingException, JsonProcessingException {
        deleteCenter(centerId);
    }

    @Transactional
    private void deleteCenter(Long centerId) {
        centerRepository.deleteById(centerId);
    }

    private <T extends IdName> T receiveAndUpdateIdNameEntity(final String receivedStr, final Class<T> clazz, final CrudRepository<T, Long> repository) {
        IdName received = new IdName();
        try {
            received = objectMapper.readValue(receivedStr, IdName.class);
            T existing = repository.findById(received.getId()).orElse(null);
            if (existing != null) {
                // update existing entity's name
                existing.setName(received.getName());
                T entity =  repository.save(existing);
                return entity;
            } else {
                try {
                    T newOne = clazz.newInstance();
                    newOne.setId(received.getId());
                    newOne.setName(received.getName());
                    if (newOne.getId() == null) throw new IllegalStateException("The entity should must have an id ! Received string : \"" + receivedStr + "\"");
                    T entity = repository.save(newOne);
                    return entity;
                } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
                    throw new AmqpRejectAndDontRequeueException("Cannot instanciate " + clazz.getSimpleName() + " class through reflection. It is a programming error.", e);
                }
            }
        } catch (IOException e) {
            LOG.error("Could not read value transmit as Subject class through RabbitMQ", e);
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR);
        }
    }

    /**
     * Receives a shanoirEvent as a json object, concerning a subject deletion
     * @param subjectIdAsString a string of the subject's id
     */
    @RabbitListener(queues = RabbitMQConfiguration.DELETE_SUBJECT_QUEUE, containerFactory = "singleConsumerFactory")
    @Transactional
    public void deleteSubject(String subjectIdAsString) throws AmqpRejectAndDontRequeueException {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        try {
            Long subjectId = Long.valueOf(subjectIdAsString);
            Set<Long> studyIds = new HashSet<>();

            // Inverse order to remove copied examination before its source (if copied)
            List<Examination> listExam = examinationRepository.findBySubjectId(subjectId);
            Collections.reverse(listExam);

            // Delete associated examinations and datasets from solr repository
            for (Examination exam : listExam) {
                examinationService.deleteById(exam.getId(), null);
                studyIds.add(exam.getStudyId());
            }

            // Update BIDS folder
            for (Study stud : studyRepository.findAllById(studyIds)) {
                bidsService.deleteBidsFolder(stud.getId(), stud.getName());
            }

            // Delete subject from datasets database
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject != null) {
                for (Tag tag : subject.getTags()) {
                    tag.getSubjects().remove(subject);
                }
                subject.getTags().clear();
                subjectRepository.save(subject);
                subjectRepository.delete(subject);
            }

        } catch (Exception e) {
            LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR + e.getMessage(), e);
        }
    }

    /**
     * Receives a shanoirEvent as a json object, concerning a subject deletion
     * @param eventAsString the task as a json string.
     */
    @RabbitListener(bindings = @QueueBinding(
            key = ShanoirEventType.DELETE_STUDY_EVENT,
            value = @Queue(value = RabbitMQConfiguration.DELETE_STUDY_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
            autoDelete = "false", durable = "true", type = ExchangeTypes.TOPIC)), containerFactory = "singleConsumerFactory"
            )
    @Transactional
    public void deleteStudy(String eventAsString) throws AmqpRejectAndDontRequeueException {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");

        try {
            ShanoirEvent event = objectMapper.readValue(eventAsString, ShanoirEvent.class);

            // Delete associated examinations and datasets from solr repository then from database
            for (Examination exam : examinationRepository.findByStudy_Id(Long.valueOf(event.getObjectId()))) {
                examinationService.deleteById(exam.getId(), null);
            }
            // also delete associated study cards
            for (StudyCard sc : studyCardRepository.findByStudyId(Long.valueOf(event.getObjectId()))) {
                studyCardRepository.delete(sc);
            }
            // also delete associated quality cards
            for (QualityCard qc : qualityCardRepository.findByStudyId(Long.valueOf(event.getObjectId()))) {
                qualityCardRepository.delete(qc);
            }

            // Delete study from datasets database
            studyRepository.deleteById(Long.valueOf(event.getObjectId()));
        } catch (Exception e) {
            LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR + e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_DATASETS_DETAILED_STORAGE_VOLUME, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public String getDetailedStudyStorageVolume(Long studyId) {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        StudyStorageVolumeDTO dto = new StudyStorageVolumeDTO(datasetService.getVolumeByFormat(studyId),
                examinationService.getExtraDataSizeByStudyId(studyId));
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            LOG.error("Error while serializing StudyVolumeStorageDTO.", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_DATASETS_TOTAL_STORAGE_VOLUME, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public String getDetailedStorageVolumeByStudy(List<Long> studyIds) {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        Map<Long, StudyStorageVolumeDTO> studyStorageVolumes = new HashMap<>();
        datasetService.getVolumeByFormatByStudyId(studyIds).forEach((id, volumeByFormat) -> {
            studyStorageVolumes.put(id, new StudyStorageVolumeDTO(volumeByFormat, examinationService.getExtraDataSizeByStudyId(id)));
        });
        try {
            return objectMapper.writeValueAsString(studyStorageVolumes);
        } catch (JsonProcessingException e) {
            LOG.error("Error while serializing HashMap<Long, StudyVolumeStorageDTO>.", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    /**
     * Iterate through a list of dataset to copy each into a new study
     * @param data The list of datasets id to copy and the studyId to copy in
     *
     * @return
     */
    @RabbitListener(queues = RabbitMQConfiguration.COPY_DATASETS_TO_STUDY_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Async
    public void copyDatasetsToStudy(final String data) {
        Map<Long, Examination> examMap = new HashMap<>();
        Map<Long, DatasetAcquisition> acqMap = new HashMap<>();
        List<Long> datasetParentIds;
        List<Long> newDatasets = new ArrayList<>();
        int countProgress = 0;
        int countProcessed = 0;
        int countAlreadyExist = 0;
        int countCopy = 0;
        int countSuccess = 0;
        int countTotal = 0;
        List<String> errors = new ArrayList<>();
        float progress = 0f;
        ShanoirEvent event = null;

        try {
            RelatedDataset dto = objectMapper.readValue(data, RelatedDataset.class);
            Long userId = dto.getUserId();
            KeycloakUtil.UserRole role = dto.getUserRole();
            Long studyId = dto.getStudyId();
            datasetParentIds = dto.getDatasetIds();
            countTotal = datasetParentIds.size();
            event = new ShanoirEvent(
                ShanoirEventType.COPY_DATASET_EVENT,
                null,
                userId,
                "Copy of dataset " + countProgress++ + "/" + countTotal + " to study [" + studyId + "].",
                ShanoirEvent.IN_PROGRESS,
                Float.valueOf(countProgress / countTotal),
                studyId
            );
            event.setId(dto.getEventId());
            event.setReport("");

            /** Check rights */
            if (!securityService.checkDatasetRelatedDatasets(dto.getDatasetIds(), userId, role)) {
                LOG.error("User {} is not allowed to copy datasets {}, copy aborted.", userId, dto.getDatasetIds());
                event.setMessage("User don't have the rights to copy these datasets, copy aborted.");
                event.setStatus(ShanoirEvent.ERROR);
                event.setProgress(-1f);
                eventService.publishEvent(event);
                return;
            }
            /* */
            switch (role) {
                case ADMIN -> SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN", userId);
                case EXPERT -> SecurityContextUtil.initAuthenticationContext("ROLE_EXPERT", userId);
                case USER -> SecurityContextUtil.initAuthenticationContext("ROLE_USER", userId);
                default -> {
                    LOG.error("User {} has an unauthorized role {}, copy aborted.", userId, role);
                    event.setMessage("User has an unauthorized role, copy aborted.");
                    event.setStatus(ShanoirEvent.ERROR);
                    event.setProgress(-1f);
                    eventService.publishEvent(event);
                    return;
                }
            }
            StudyExaminationsDTO propagatedExams = new StudyExaminationsDTO(studyId);
            List<CopyReport> cvsReports = new ArrayList<>();
            for (Long datasetParentId : datasetParentIds) {
                progress += 1f / countTotal;
                event.setMessage("Copy of dataset [" + datasetParentId + "] to study [" + studyId + "]: " + countProgress++ + "/" + countTotal);
                event.setProgress(progress);
                event.setReport(buildReport(datasetParentIds, countProcessed, countAlreadyExist, countCopy, countSuccess, errors));
                eventService.publishEvent(event);

                LOG.info("[CopyDatasets] Start copy for dataset " + datasetParentId + " to study " + studyId);
                Long dsCount = datasetRepository.countDatasetsBySourceIdAndStudyId(datasetParentId, studyId);

                if (dsCount != 0) {
                    LOG.info("[CopyDatasets] Dataset already exists in this study, copy aborted.");
                    countAlreadyExist++;
                } else {
                    try {
                        DatasetCopyService.DatasetCopyResult result = datasetCopyService.moveDataset(datasetParentId, studyId, dto.getSubjectMapping(), examMap, acqMap, userId);
                        Long newDsId = result.getNewDsId();
                        countProcessed += result.getCountProcessed();
                        countSuccess += result.getCountSuccess();
                        countCopy += result.getCountCopy();
                        LOG.info("countProcessed : " + countProcessed);
                        if (newDsId != null) newDatasets.add(newDsId);
                        propagatedExams.addExam(result.getExaminationId(), result.getCenterId(), result.getSubjectId());
                        CopyReport cvsReport = new CopyReport();
                        cvsReport.setSourceDatasetId(datasetParentId);
                        cvsReport.setTargetDatasetId(newDsId);
                        cvsReport.setSubjectNewName(result.getSubjectName());
                        cvsReports.add(cvsReport);
                    } catch (DatasetCopyService.NotFoundSubjectIdException e) {
                        LOG.error("[CopyDatasets] No mapping found for subject with id = " + e.getSubjectId() + ", copy aborted for dataset " + datasetParentId);
                        errors.add("No mapping found for subject with id = " + e.getSubjectId() + ", copy aborted for dataset " + datasetParentId
                                + ". The csv input might be associating the wrong subject id to the dataset.");
                    } catch (DatasetCopyService.NotFoundDatasetIdException e) {
                        LOG.error("[CopyDatasets] No dataset found with id = " + e.getDatasetId() + ", copy aborted for dataset " + datasetParentId);
                        errors.add("No dataset found with id = " + e.getDatasetId() + ", copy aborted for dataset " + datasetParentId);
                    } catch (JsonProcessingException e) {
                        LOG.error("[CopyDatasets] Error processing json during the copy of dataset " + datasetParentId, e);
                        errors.add("Error processing json during the copy of dataset " + datasetParentId + ": " + e.getMessage());
                    } catch (Exception e) {
                        LOG.error("[CopyDatasets] Unexpected error during the copy of dataset " + datasetParentId, e);
                        errors.add("Unexpected error during the copy of dataset " + datasetParentId + ": " + e.getMessage());
                    }
                }
            }
            if (!cvsReports.isEmpty()) {
                csvCopyService.writeReportTsvFile(cvsReports, event.getId());
            }
            propagateExaminations(propagatedExams);

            event.setMessage("Copy ended");
            event.setStatus(ShanoirEvent.SUCCESS);
            event.setProgress(1.0f);
            event.setReport(buildReport(datasetParentIds, countProcessed, countAlreadyExist, countCopy, countSuccess, errors));
            eventService.publishEvent(event);
            if (!newDatasets.isEmpty())
                solrService.indexDatasets(newDatasets);

        } catch (Exception e) {
            if (event != null) {
                event.setMessage("[CopyDatasets] Error during the copy of dataset.");
                event.setStatus(ShanoirEvent.ERROR);
                event.setProgress(-1f);
                event.setReport(e.getMessage());
                eventService.publishEvent(event);
            }
            LOG.error("Something went wrong during the copy. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

    private void propagateExaminations(StudyExaminationsDTO propagatedExams) throws ShanoirException {
        Long userId = KeycloakUtil.getTokenUserId();
        try {
            eventService.publishEvent(
                    new ShanoirEvent(
                            ShanoirEventType.CREATE_EXAMINATIONS_EVENT,
                            null,
                            userId,
                            objectMapper.writeValueAsString(propagatedExams),
                            ShanoirEvent.SUCCESS,
                            propagatedExams.getStudyId()));
        } catch (JsonProcessingException e) {
            throw new ShanoirException("Error processing json during the propagation of examinations after dataset copy.", e);
        }
    }

    private String buildReport(List<Long> datasetParentIds, int countProcessed, int countAlreadyExist,
            int countCopy, int countSuccess, List<String> errors) {
        return "Copy successful for " + countSuccess + "/" + datasetParentIds.size() + " datasets.\n"
                + countCopy + " were already copied datasets.\n"
                + countAlreadyExist + " already existed in destination study.\n"
                + countProcessed + " are processed datasets and cannot be copied.\n"
                + (!errors.isEmpty() ? "Errors: " + String.join("\n", errors) : "");
    }

}
