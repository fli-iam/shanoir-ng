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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.service.AcquisitionEquipmentService;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.service.CenterService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.message.CreateCenterForStudyMessage;
import org.shanoir.ng.shared.message.CreateEquipmentForCenterMessage;
import org.shanoir.ng.shared.quality.SubjectQualityTagDTO;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RabbitMQStudiesService {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQStudiesService.class);

	private static final String RABBIT_MQ_ERROR = "Something went wrong deserializing the object.";

	private static final String DELIMITER = ":";

    @Autowired
    private StudyRepository studyRepo;

    @Autowired
    private StudyService studyService;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private CenterService centerService;
	
	@Autowired
	private DataUserAgreementService dataUserAgreementService;
	
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	ShanoirEventService eventService;

	@Autowired
	private AcquisitionEquipmentService acquisitionEquipmentService;

	/**
	 * Receives a shanoirEvent as a json object, concerning an examination creation
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
			key = ShanoirEventType.CREATE_EXAMINATION_EVENT,
			value = @Queue(value = RabbitMQConfiguration.EXAMINATION_STUDY_QUEUE, durable = "true"),
			exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
			autoDelete = "false", durable = "true", type=ExchangeTypes.TOPIC)), containerFactory = "multipleConsumersFactory"
			)
	@RabbitHandler
	@Transactional
	public void linkExamination(final String eventStr) {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {
			ShanoirEvent event =  mapper.readValue(eventStr, ShanoirEvent.class);
			Long examinationId = Long.valueOf(event.getObjectId());
			Long studyId = event.getStudyId();
			String message = event.getMessage();
			Pattern pat = Pattern.compile("centerId:(\\d+);subjectId:(\\d+)");
			Matcher mat = pat.matcher(message);
			
			Long centerId = null;
			Long subjectId = null;
			if (mat.matches()) {
				centerId = Long.valueOf(mat.group(1));
				subjectId = Long.valueOf(mat.group(2));
			} else {
				LOG.error("Something wrong happend while updating study examination list.");
				throw new ShanoirException("Could not read subject ID and center ID from event message");
			}

            this.studyService.addExaminationToStudy(examinationId, studyId, centerId, subjectId);

        } catch (Exception e) {
            LOG.error("Could not index examination on given study ", e);
            throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
        }
    }

    /**
     * Receives a shanoirEvent as a json object, concerning an examination creation
     * @param eventStr the event as a json string.
     */
    @RabbitListener(queues = RabbitMQConfiguration.EXAMINATION_STUDY_DELETE_QUEUE, containerFactory = "singleConsumerFactory")
    @Transactional
    public void deleteExaminationStudy(final String eventStr) {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ShanoirEvent event =  objectMapper.readValue(eventStr, ShanoirEvent.class);
            Long examinationId = Long.valueOf(event.getObjectId());
            Long studyId = Long.valueOf(event.getStudyId());
            this.studyService.deleteExamination(examinationId, studyId);
        } catch (Exception e) {
            LOG.error("Could not index examination on given study ", e);
            throw new AmqpRejectAndDontRequeueException("Something went wrong deserializing the event." + e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_NAME_QUEUE, containerFactory = "singleConsumerFactory")
    @Transactional
    public String getStudyName(final long studyId) {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        Study study = this.studyRepo.findById(studyId).get();
        if (study != null) {
            return study.getName();
        }
        return null;
    }

    @RabbitListener(queues = RabbitMQConfiguration.STUDY_ANONYMISATION_PROFILE_QUEUE, containerFactory = "singleConsumerFactory")
    @Transactional
    public String getStudyAnonymisationProfile(final long studyId) {
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        Study study = this.studyRepo.findById(studyId).get();
        if (study != null) {
            return study.getProfile().getProfileName();
        }
        return null;
    }

	/**
	 * Receives a json object, concerning a study subscription
	 * @param commandArrStr the studyUser as a json string.
	 */
	@RabbitListener(queues = RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE, containerFactory = "singleConsumerFactory")
	@Transactional
	public boolean studySubscription(final String studyStr) {
		SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
		try {
			ShanoirEvent event =  mapper.readValue(studyStr, ShanoirEvent.class);
			Long userId = event.getUserId();
			Long studyId = Long.valueOf(event.getObjectId());
			// Get the study
			Study studyToUpdate = studyRepo.findById(studyId).orElseThrow();
			
			for (StudyUser su : studyToUpdate.getStudyUserList()) {
				if (su.getUserId().equals(userId)) {
					// user already exists on study
					return true;
				}
			}
			
			// Create a new StudyUser
			StudyUser subscription = new StudyUser();
			subscription.setStudy(studyToUpdate);
			subscription.setUserId(userId);
			subscription.setReceiveNewImportReport(false);
			subscription.setReceiveStudyUserReport(false);
			subscription.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD));
			subscription.setUserName(event.getMessage());

			if (studyToUpdate.getDataUserAgreementPaths() != null && !studyToUpdate.getDataUserAgreementPaths().isEmpty()) {
				subscription.setConfirmed(false);
				dataUserAgreementService.createDataUserAgreementForUserInStudy(studyToUpdate, subscription.getUserId());
			} else {
				subscription.setConfirmed(true);
			}
			studyService.addStudyUserToStudy(subscription, studyToUpdate);
			eventService.publishEvent(event);
			return true;
		} catch (Exception e) {
			LOG.error("Could not directly subscribe a user to the study: ", e);
			return false;
		}
	}
	
	@Transactional
	@RabbitListener(queues = RabbitMQConfiguration.STUDIES_SUBJECT_STUDY_STUDY_CARD_TAG, containerFactory = "singleConsumerFactory")
	@RabbitHandler
	public void receiveSubjectStudyStudyCardTagUpdate(final String messageStr) {
		try {
		    LOG.info(messageStr);
			List<SubjectQualityTagDTO> subjectStudyCardTagList =
					mapper.readValue(messageStr, new TypeReference<List<SubjectQualityTagDTO>>(){});
			// build a id -> dto map
			Map<Long, SubjectQualityTagDTO> dtoMap = new HashMap<>();
			for (SubjectQualityTagDTO dto : subjectStudyCardTagList) {
			    dtoMap.put(dto.getSubjectId(), dto);
			}
			// get subjects from db
			Iterable<Subject> dbList = subjectRepository.findAllById(dtoMap.keySet());
			// update subjects
			for (Subject subject : dbList) {
			    subject.setQualityTag(dtoMap.get(subject.getId()).getTag());
			}
			// save
			subjectRepository.saveAll(dbList);			
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR, e);
		}
	}

	@RabbitListener(queues = RabbitMQConfiguration.CENTER_CREATE_QUEUE, containerFactory = "singleConsumerFactory")
	@RabbitHandler
	public String createCenter(final String messageStr) {
		try {
			SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
			CreateCenterForStudyMessage message = mapper.readValue(messageStr, CreateCenterForStudyMessage.class);
			Center center = findOrCreateOrAddCenterByInstitutionDicom(message.getStudyId(), message.getInstitutionDicom());
			if (center != null) {
				Long studyCenterId = center.getStudyCenterList().stream()
						.filter(sc -> sc.getStudy().getId().equals(message.getStudyId()))
						.findFirst().orElseThrow().getId();
				String returnMessage = center.getId() + DELIMITER + studyCenterId;
				return returnMessage;
			} else {
				LOG.error("Error while creating a new center.");
				return null;
			}
		} catch (Exception e) {
			LOG.error("Error while creating a new center: ", e);
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	@Transactional
	private Center findOrCreateOrAddCenterByInstitutionDicom(Long studyId, InstitutionDicom institutionDicom) {
		try {
			return centerService.findOrCreateOrAddCenterByInstitutionDicom(studyId, institutionDicom, false);
		} catch (EntityNotFoundException e) {
			LOG.error("Error while creating a new center: ", e);
		}
		return null;
	}

	@RabbitListener(queues = RabbitMQConfiguration.ACQUISITION_EQUIPMENT_CREATE_QUEUE, containerFactory = "singleConsumerFactory")
	@RabbitHandler
	public Long createEquipment(final String messageStr) {
		try {
			SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
			CreateEquipmentForCenterMessage message = mapper.readValue(messageStr, CreateEquipmentForCenterMessage.class);
			AcquisitionEquipment equipment = createEquipmentByEquipmentDicom(message.getCenterId(), message.getEquipmentDicom());
			if (equipment != null) {
				return equipment.getId();
			} else {
				LOG.error("Error while creating a new equipment.");
				return null;
			}
		} catch (JsonProcessingException e) {
			LOG.error("Error while creating a new equipment: ", e);
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	@Transactional
	private AcquisitionEquipment createEquipmentByEquipmentDicom(Long centerId, EquipmentDicom equipmentDicom) {
		try {
			return acquisitionEquipmentService.saveNewAcquisitionEquipment(centerId, equipmentDicom, false);
		} catch (Exception e) {
			LOG.error("Error while creating a new equipment: ", e);
		}
		return null;
	}

}
