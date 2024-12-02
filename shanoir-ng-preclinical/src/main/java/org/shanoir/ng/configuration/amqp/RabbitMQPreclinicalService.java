package org.shanoir.ng.configuration.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RabbitMQPreclinicalService {

    private static final String RABBIT_MQ_ERROR = "Something went wrong deserializing the event.";
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnimalSubjectService animalSubjectService;

    @Autowired
    private SubjectPathologyService subjectPathologyService;

    @Autowired
    private SubjectTherapyService subjectTherapyService;

    @Autowired
    private ShanoirEventService eventService;

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQPreclinicalService.class);

    /**
     * Receives a shanoirEvent as a json object, concerning a subject deletion
     * @param subjectIdAsStr the subject's id to delete, as string
     */
    @RabbitListener(queues = RabbitMQConfiguration.DELETE_ANIMAL_SUBJECT_QUEUE)
    @Transactional
    public void deleteAnimalSubject(String subjectIdAsStr) throws AmqpRejectAndDontRequeueException {
        SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
        try {
            Long subjectId = Long.valueOf(subjectIdAsStr);

            AnimalSubject animalSubject = animalSubjectService.getBySubjectId(subjectId);

            if(animalSubject == null){
                return;
            }
            Long id = animalSubject.getId();

            subjectPathologyService.deleteByAnimalSubject(animalSubject);
            subjectTherapyService.deleteByAnimalSubject(animalSubject);
            animalSubjectService.deleteBySubjectId(subjectId);

            LOG.info("Animal subject [{}] has been deleted following deletion of subject [{}]", id, subjectId);

        } catch (Exception e) {
            LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR + e.getMessage(), e);
        }
    }

}
