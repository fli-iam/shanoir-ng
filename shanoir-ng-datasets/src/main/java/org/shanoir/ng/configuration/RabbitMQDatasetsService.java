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
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetCopyService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dataset.RelatedDataset;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.model.AcquisitionEquipment;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
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
    private BIDSService bidsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudyService studyService;

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQDatasetsService.class);

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_DATASET, durable = "true"),
            exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
            autoDelete = "false", durable = "true", type = ExchangeTypes.FANOUT)), containerFactory = "multipleConsumersFactory"
    )
    public void receiveMessage(String commandArrStr) {
        listener.receiveStudyUsers(commandArrStr);
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_NAME_UPDATE_QUEUE, containerFactory = "singleConsumerFactory")
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
    public void receiveSubjectUpdate(final String subjectStr) {
        try {
            manageSubjectUpdate(subjectStr);
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
        Subject received = objectMapper.readValue(subjectStr, Subject.class);
        received = subjectRepository.save(received);
        // Update BIDS
        Set<Long> studyIds = new HashSet<>();
        for (Examination exam : examinationRepository.findBySubjectId(received.getId())) {
            studyIds.add(exam.getStudyId());
        }
        for (Study stud : studyRepository.findAllById(studyIds)) {
            bidsService.deleteBidsFolder(stud.getId(), stud.getName());
        }
        // Update solr references
        List<Long> subjectIdList = new ArrayList<Long>();
        subjectIdList.add(received.getId());
        try {
            solrService.updateSubjectsAsync(subjectIdList);
        } catch (Exception e) {
            LOG.error("Solr update failed for subjects {}", subjectIdList, e);
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
            subjectRepository.deleteById(subjectId);

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
    @Transactional
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
        float progress = 0f;
        ShanoirEvent event = null;
        try {
            RelatedDataset dto = objectMapper.readValue(data, RelatedDataset.class);
            SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
            Long userId = dto.getUserId();
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
            event.setReport("");

            for (Long datasetParentId : datasetParentIds) {
                progress += 1f / countTotal;
                event.setMessage("Copy of dataset [" + datasetParentId + "] to study [" + studyId + "]: " + countProgress++ + "/" + countTotal);
                event.setProgress(progress);
                eventService.publishEvent(event);

                LOG.info("[CopyDatasets] Start copy for dataset " + datasetParentId + " to study " + studyId);
                Long dsCount = datasetRepository.countDatasetsBySourceIdAndStudyId(datasetParentId, studyId);
                Dataset datasetParent = datasetService.findById(datasetParentId);

                if (datasetParent.getSource() != null) {
                    LOG.info("[CopyDatasets] Selected dataset is a copy, please pick the original dataset.");
                    countCopy++;
                } else if (dsCount != 0) {
                    LOG.info("[CopyDatasets] Dataset already exists in this study, copy aborted.");
                    countAlreadyExist++;

                } else {
                    Object[] result = datasetCopyService.moveDataset(datasetParent, studyId, examMap, acqMap, userId);
                    Long newDsId = (Long) result[0];
                    countProcessed += (int) result[1];
                    countSuccess += (int) result[2];
                    LOG.info("countProcessed : " + countProcessed);
                    if (newDsId != null)
                        newDatasets.add(newDsId);
                }
            }

            event.setMessage("Copy successful for " + countSuccess + "/" + countTotal + " datasets to study [" + studyId + "].\n"
                    + countCopy + " were already copied datasets.\n"
                    + countAlreadyExist + " already existed in destination study.\n"
                    + countProcessed + " are processed datasets and cannot be copied.");
            event.setStatus(ShanoirEvent.SUCCESS);
            event.setProgress(1.0f);
            eventService.publishEvent(event);
            if (newDatasets.size() > 0)
                solrService.indexDatasets(newDatasets);

        } catch (Exception e) {
            if (event != null) {
                event.setMessage("[CopyDatasets] Error during the copy of dataset.");
                event.setStatus(ShanoirEvent.ERROR);
                event.setProgress(-1f);
                eventService.publishEvent(event);
            }
            LOG.error("Something went wrong during the copy. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }

}
