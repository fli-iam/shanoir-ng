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

import org.shanoir.ng.study.rights.ampq.StudyUserListener;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration.
 */
@Configuration
public class RabbitMQConfiguration {
	
	@Autowired
	private StudyUserListener listener;

	@Bean
	public static org.springframework.amqp.core.Queue studyUserQueue() {
		return new org.springframework.amqp.core.Queue("study-user-queue-dataset", true);
	}
	
	@Bean
	public static org.springframework.amqp.core.Queue importerQueue() {
		return new org.springframework.amqp.core.Queue("importer-queue-dataset", true);
	}
	
	@Bean
    public FanoutExchange fanout() {
        return new FanoutExchange("study-user-exchange", true, false);
    }	

	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = "study-user-queue-dataset", durable = "true"),
	        exchange = @Exchange(value = "study-user-exchange", ignoreDeclarationExceptions = "true", 
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT))
	)
    public void receiveMessage(String commandArrStr) throws AmqpRejectAndDontRequeueException  {
		listener.receiveMessageImport(commandArrStr);
    }

}
