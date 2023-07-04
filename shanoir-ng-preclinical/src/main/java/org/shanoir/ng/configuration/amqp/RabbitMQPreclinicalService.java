package org.shanoir.ng.configuration.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.repository.AnimalSubjectRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
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

import java.util.*;

@Component
public class RabbitMQPreclinicalService {

    private static final String RABBIT_MQ_ERROR = "Something went wrong deserializing the event.";
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnimalSubjectRepository repository;

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQPreclinicalService.class);

    /**
     * Receives a shanoirEvent as a json object, concerning a subject deletion
     * @param eventAsString the task as a json string.
     */
    @RabbitListener(bindings = @QueueBinding(
            key = ShanoirEventType.DELETE_SUBJECT_EVENT,
            value = @Queue(value = RabbitMQConfiguration.DELETE_SUBJECT_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitMQConfiguration.EVENTS_EXCHANGE, ignoreDeclarationExceptions = "true",
                    autoDelete = "false", durable = "true", type= ExchangeTypes.TOPIC))
    )
    @Transactional
    public void deleteAnimalSubject(String eventAsString) throws AmqpRejectAndDontRequeueException {
        SecurityContextUtil.initAuthenticationContext("ADMIN_ROLE");
        try {

            ShanoirEvent event = objectMapper.readValue(eventAsString, ShanoirEvent.class);

            Optional<AnimalSubject> animal = repository.findById(Long.valueOf(event.getObjectId()));

            if(animal.isEmpty()){
                return;
            }

            Long id = animal.get().getId();

            repository.deleteById(id);
            LOG.info("Animal subject [{}] has been deleted following deletion of subject [{}]", id, id);

        } catch (Exception e) {
            LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(RABBIT_MQ_ERROR + e.getMessage(), e);
        }
    }

}
