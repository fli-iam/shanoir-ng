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

import java.io.IOException;

import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@Profile("!test")
public class RabbitMQConfiguration {

	private static final String MS_USERS_TO_MS_STUDIES_USER_DELETE = "ms_users_to_ms_studies_user_delete";

	private static final String SHANOIR_EVENTS_QUEUE = "shanoir_events_queue";

	private static final Logger LOG = LoggerFactory.getLogger(RabbitMQConfiguration.class);

	@Autowired
	ShanoirEventsService eventsService;
	
    @Bean
    public static org.springframework.amqp.core.Queue getMSUsersToMSStudiesUserDelete() {
    		return new org.springframework.amqp.core.Queue(MS_USERS_TO_MS_STUDIES_USER_DELETE, true);
    }

    @Bean
    public static org.springframework.amqp.core.Queue getShanoirEventsQueue() {
    		return new org.springframework.amqp.core.Queue(SHANOIR_EVENTS_QUEUE, true);
    }

	@Bean
	public FanoutExchange fanoutEvents() {
	    return new FanoutExchange("shanoir-events-exchange");
	}

	/**
	 * Receives a shanoirEvent as a json object, thus create a event in the queue
	 * @param commandArrStr the task as a json string.
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = SHANOIR_EVENTS_QUEUE, durable = "true"),
	        exchange = @Exchange(value = "shanoir-events-exchange", ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT))
	)
	public void receiveEvent(String eventAsString) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		try {
			ShanoirEvent event = mapper.readValue(eventAsString, ShanoirEvent.class);
			eventsService.addEvent(event);
		} catch (IOException e) {
			LOG.error("Something went wrong deserializing the event. {}", e.getMessage());
		}
	}
}
