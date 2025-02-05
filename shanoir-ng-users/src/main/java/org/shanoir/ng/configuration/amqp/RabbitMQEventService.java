package org.shanoir.ng.configuration.amqp;

import java.util.List;

import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RabbitMQEventService {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQEventService.class);

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public ShanoirEventsService service;

    @RabbitListener(queues = RabbitMQConfiguration.EXECUTION_MONITORING_TASK, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public String getExecutionMonitoringEventByObjectId(Long objectId) {

        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");

        List<ShanoirEvent> events = service.getEventsByObjectIdAndTypeIn(objectId.toString(), ShanoirEventType.EXECUTION_MONITORING_EVENT);

        try {
            return objectMapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            LOG.error("Error while serializing ShanoirEvent list.", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

}
