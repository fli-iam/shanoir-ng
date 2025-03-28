package org.shanoir.ng.configuration.amqp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.quality.SubjectStudyQualityTagDTO;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RabbitMQStudiesService {
    
    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQStudiesService.class);

    private static final String RABBIT_MQ_ERROR = "Something went wrong deserializing the object.";

    @Autowired
    private StudyRepository studyRepo;

    @Autowired
    private StudyService studyService;

    @Autowired
    private SubjectStudyRepository subjectStudyRepository;
    
    @Autowired
    private DataUserAgreementService dataUserAgreementService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ShanoirEventService eventService;
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
            ShanoirEvent event =  objectMapper.readValue(eventStr, ShanoirEvent.class);
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
            ShanoirEvent event =  objectMapper.readValue(studyStr, ShanoirEvent.class);
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
            List<SubjectStudyQualityTagDTO> subjectStudyStudyCardTagList =
                    objectMapper.readValue(messageStr, new TypeReference<List<SubjectStudyQualityTagDTO>>() { });
            // build a id -> dto map
            Map<Long, SubjectStudyQualityTagDTO> dtoMap = new HashMap<>();
            for (SubjectStudyQualityTagDTO dto : subjectStudyStudyCardTagList) {
                dtoMap.put(dto.getSubjectStudyId(), dto);
            }
            // get subject studies from db
            Iterable<SubjectStudy> dbList = subjectStudyRepository.findAllById(dtoMap.keySet());
            // update subject studies
            for (SubjectStudy subjectStudy : dbList) {
                subjectStudy.setQualityTag(dtoMap.get(subjectStudy.getId()).getTag());
            }
            // save
            subjectStudyRepository.saveAll(dbList);            
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR, e);
        }
    }

}
