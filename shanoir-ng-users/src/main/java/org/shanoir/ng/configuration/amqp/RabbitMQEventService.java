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
    private ObjectMapper objectMapper;

    @Autowired
    private ShanoirEventsService service;

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
