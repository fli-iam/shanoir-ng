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

package org.shanoir.ng.shared.event;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to send every event created.
 * @author fli
 *
 */
@Service
public class ShanoirEventService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper mapper;

    private static final Logger LOG = LoggerFactory.getLogger(ShanoirEventService.class);

    /**
     * Publishes an event to user microservice.
     * @param event
     */
    public void publishEvent(ShanoirEvent event) {
        StringBuilder builder = new StringBuilder("Event:[")
                .append("id=").append(event.getId()).append(";")
                .append("user_id=").append(event.getUserId()).append(";")
                .append("event_type=").append(event.getEventType()).append(";")
                .append("object_id=").append(event.getObjectId()).append(";")
                .append("message=").append(event.getMessage()).append(";")
                .append("report=").append(event.getReport()).append(";")
                .append("status=").append(event.getStatus()).append(";")
                .append("progress=").append(event.getProgress()).append("]");
        LOG.info(builder.toString());
        try {
            String str = mapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.EVENTS_EXCHANGE, event.getEventType(), str);
        } catch (JsonProcessingException e) {
            LOG.error("Error while sending event: event {}, user: {}, reference: {}", event.getEventType(), event.getUserId(), event.getObjectId());
            LOG.error("Thrown exception: {}", e);
        }
    }

    public void publishEvent(ShanoirEvent event, String message, Float progress) {
        event.setMessage(message);
        event.setProgress(progress);
        this.publishEvent(event);
    }

    public void publishErrorEvent(ShanoirEvent event, String message) {
        this.publishEvent(event, message, -1f, ShanoirEvent.ERROR);
    }

    public void publishSuccessEvent(ShanoirEvent event, String message) {
        this.publishEvent(event, message, 1f, ShanoirEvent.SUCCESS);
    }

    private void publishEvent(ShanoirEvent event, String message, Float progress, int status) {
        event.setStatus(status);
        this.publishEvent(event, message, progress);
    }
}
