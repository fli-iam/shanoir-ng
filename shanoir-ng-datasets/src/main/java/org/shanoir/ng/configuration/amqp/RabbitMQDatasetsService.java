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

package org.shanoir.ng.configuration.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.bids.service.BIDSService;
import org.shanoir.ng.dataset.dto.StudyStorageVolumeDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetCopyService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dataset.RelatedDataset;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.model.*;
import org.shanoir.ng.shared.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private DatasetAcquisitionService datasetAcquisitionService;

	@Autowired
	private ExaminationService examinationService;

	@Autowired
	ShanoirEventService eventService;

	@Autowired
	private ExaminationRepository examinationRepository;
	
	@Autowired
	private StudyCardRepository studyCardRepository;

	@Autowired
	private BIDSService bidsService;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	EntityManager entityManager;

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQDatasetsService.class);

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_DATASET, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT))
	)
	public void receiveMessage(String commandArrStr) {
		listener.receiveMessageImport(commandArrStr);
	}

	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_NAME_UPDATE_QUEUE)
	@RabbitHandler
	public void receiveStudyNameUpdate(final String studyStr) {
		try {
			Study received = objectMapper.readValue(studyStr, Study.class);
			bidsService.deleteBidsFolder(received.getId(), null);
			Study stud = receiveAndUpdateIdNameEntity(studyStr, Study.class, studyRepository);

			// TAGS
			if (stud.getTags() != null) {
				stud.getTags().clear();
			} else {
				stud.setTags(new ArrayList<>());
			}
			if (received.getTags() != null) {
				stud.getTags().addAll(received.getTags());
			}
			for (Tag tag : stud.getTags()) {
				tag.setStudy(stud);
			}
			if (stud.getId() == null) throw new IllegalStateException("The entity should must have an id ! Received string : \"" + studyStr + "\"");
			Study studyDb = this.studyRepository.save(stud); 

			// SUBJECT_STUDY
			if (stud.getSubjectStudyList() != null) {
				stud.getSubjectStudyList().clear();
			} else {
				stud.setSubjectStudyList(new ArrayList<>());
			}
			if (received.getSubjectStudyList() != null) {
				stud.getSubjectStudyList().addAll(received.getSubjectStudyList());
			}
			for (SubjectStudy sustu : stud.getSubjectStudyList()) {
				sustu.setStudy(stud);
				for (Tag tag : sustu.getTags()) {
					if (tag.getId() == null) {
						Tag dbTag = studyDb.getTags().stream().filter(upTag -> 
								upTag.getColor().equals(tag.getColor()) && upTag.getName().equals(tag.getName())
						).findFirst().orElse(null);
						if (dbTag != null) {
							tag.setId(dbTag.getId());							
						} else {
							throw new IllegalStateException("Cannot link a new tag to a subject-study, this tag does not exist in the study");
						}
					}
				}
			}
			if (stud.getId() == null) throw new IllegalStateException("The entity should must have an id ! Received string : \"" + studyStr + "\"");
			this.studyRepository.save(stud);
			List<Long> subjectIds = new ArrayList<>();
			stud.getSubjectStudyList().forEach(subStu -> subjectIds.add(subStu.getSubject().getId()));
			updateSolr(subjectIds);
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR, e);
		}
	}

	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.SUBJECT_NAME_UPDATE_QUEUE)
	@RabbitHandler
	public boolean receiveSubjectNameUpdate(final String subjectStr) {		
		Subject su = receiveAndUpdateIdNameEntity(subjectStr, Subject.class, subjectRepository);
		try {
			if (su != null && su.getId() == null) throw new IllegalStateException("The subject should must have an id !");
			Subject received = objectMapper.readValue(subjectStr, Subject.class);
	
			// SUBJECT_STUDY
			if (su.getSubjectStudyList() != null) {
				su.getSubjectStudyList().clear();
			} else {
				su.setSubjectStudyList(new ArrayList<>());
			}
			if (received.getSubjectStudyList() != null) {
				su.getSubjectStudyList().addAll(received.getSubjectStudyList());
			}
			for (SubjectStudy sustu : su.getSubjectStudyList()) {
				sustu.setSubject(su);
			}
			if (su.getId() == null) throw new IllegalStateException("The entity should must have an id ! Received string : \"" + subjectStr + "\"");
			subjectRepository.save(su);
			
			// Update solr references
			List<Long> subjectIdList = new ArrayList<Long>();
			subjectIdList.add(su.getId());
			updateSolr(subjectIdList);
			
			// Update BIDS
			Set<Long> studyIds = new HashSet<>();

			for (Examination exam : examinationRepository.findBySubjectId(received.getId())) {
				studyIds.add(exam.getStudyId());
			}
			for (Study stud : studyRepository.findAllById(studyIds)) {
				bidsService.deleteBidsFolder(stud.getId(), stud.getName());
			}
			return true;
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR, e);
		}
	}

	/**
	 * Updates all the solr references for this subject.
	 * @param subjectIds the subject ID updated
	 */
	private void updateSolr(final List<Long> subjectIds) throws SolrServerException, IOException {
		Set<Long> datasetsToUpdate = new HashSet<>();
		for (Examination exam : examinationRepository.findBySubjectIdIn(subjectIds)) {
			for (DatasetAcquisition acq : exam.getDatasetAcquisitions()) {
				for (Dataset ds : acq.getDatasets()) {
					datasetsToUpdate.add(ds.getId());
				}
			}
		}
		if (!CollectionUtils.isEmpty(datasetsToUpdate)) {
			this.solrService.indexDatasets(new ArrayList<>(datasetsToUpdate));
		}
	}

	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_UPDATE_QUEUE)
	@RabbitHandler
	public void receiveAcEqUpdate(final String acEqStr) {
		receiveAndUpdateIdNameEntity(acEqStr, AcquisitionEquipment.class, acquisitionEquipmentRepository);
	}

	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.CENTER_NAME_UPDATE_QUEUE)
	@RabbitHandler
	public void receiveCenterNameUpdate(final String centerStr) {
		receiveAndUpdateIdNameEntity(centerStr, Center.class, centerRepository);
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
				} catch ( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
					throw new AmqpRejectAndDontRequeueException("Cannot instanciate " + clazz.getSimpleName() + " class through reflection. It is a programming error.", e);
				}
			}
		} catch (IOException e) {
			LOG.error("Could not read value transmit as Subject class through RabbitMQ", e);
			throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR);
		}
	}

	/**
	 * Receives a shanoirEvent as a json object, concerning a dataset acquisition to create
	 * @param studyStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.CREATE_DATASET_ACQUISITION_EVENT,
			value = @Queue(value = RabbitMQConfiguration.CREATE_DATASET_ACQUISITION_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional(isolation = Isolation.READ_UNCOMMITTED,  propagation = Propagation.REQUIRES_NEW)
	public void createDatasetAcquisition(final String studyStr) {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {
			ShanoirEvent event =  objectMapper.readValue(studyStr, ShanoirEvent.class);
			DatasetAcquisition acq = datasetAcquisitionService.findById(Long.valueOf(event.getObjectId()));
			List<Long> datasetIds = new ArrayList<>();
			if (acq != null) {
				for (Dataset ds : acq.getDatasets()) {
					datasetIds.add(ds.getId());
				}
			}
			solrService.indexDatasets(datasetIds);
		} catch (Exception e) {
			LOG.error("Could not index datasets while creating new Dataset acquisition: ", e);
			throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR + e.getMessage());
		}
	}


	/**
         * Receives a shanoirEvent as a json object, concerning a subject deletion
         * @param eventAsString the task as a json string.
         */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.DELETE_SUBJECT_EVENT,
			value = @Queue(value = RabbitMQConfiguration.DELETE_SUBJECT_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional
	public void deleteSubject(String eventAsString) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {

			ShanoirEvent event = objectMapper.readValue(eventAsString, ShanoirEvent.class);
			Set<Long> studyIds = new HashSet<>();

			// Delete associated examinations and datasets from solr repository
			for (Examination exam : examinationRepository.findBySubjectId(Long.valueOf(event.getObjectId()))) {
				examinationService.deleteFromRabbit(exam);
				studyIds.add(exam.getStudyId());
			}
			
			// Update BIDS folder
			for (Study stud : studyRepository.findAllById(studyIds)) {
				bidsService.deleteBidsFolder(stud.getId(), stud.getName());
			}
			
			// Delete subject from datasets database
			subjectRepository.deleteById(Long.valueOf(event.getObjectId()));
			
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
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC))
			)
	@Transactional
	public void deleteStudy(String eventAsString) throws AmqpRejectAndDontRequeueException {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");

		try {
			ShanoirEvent event = objectMapper.readValue(eventAsString, ShanoirEvent.class);

			// Delete associated examinations and datasets from solr repository then from database
			for (Examination exam : examinationRepository.findByStudy_Id(Long.valueOf(event.getObjectId()))) {
				examinationService.deleteFromRabbit(exam);
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

	@RabbitListener(queues = RabbitMQConfiguration.STUDY_DATASETS_DETAILED_STORAGE_VOLUME)
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

	@RabbitListener(queues = RabbitMQConfiguration.STUDY_DATASETS_TOTAL_STORAGE_VOLUME)
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
	@RabbitListener(queues = RabbitMQConfiguration.COPY_DATASETS_TO_STUDY_QUEUE)
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
					Float.valueOf(countProgress/countTotal)
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

				if (datasetParent.getSourceId() != null) {
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

			event.setMessage("Copy successful for " + countSuccess + "/" + countTotal + " datasets to study [" + studyId + "].\n" +
					countCopy + " were already copied datasets.\n" +
					countAlreadyExist + " already existed in destination study.\n" +
					countProcessed + " are processed datasets and cannot be copied.");
			event.setStatus(ShanoirEvent.SUCCESS);
			event.setProgress(1.0f);
			eventService.publishEvent(event);
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
