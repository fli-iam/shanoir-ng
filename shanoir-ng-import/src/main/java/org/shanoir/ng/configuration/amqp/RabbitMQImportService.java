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

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.study.rights.ampq.StudyUserListener;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ configuration.
 */
@Component
public class RabbitMQImportService {
	
	@Autowired
	private StudyUserListener listener;
	
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = RabbitMQConfiguration.STUDY_USER_QUEUE_IMPORT, durable = "true"),
	        exchange = @Exchange(value = RabbitMQConfiguration.STUDY_USER_EXCHANGE, ignoreDeclarationExceptions = "true",
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT))
	)
	public void receiveMessage(String commandArrStr) {
		listener.receiveMessageImport(commandArrStr);
	}

}
