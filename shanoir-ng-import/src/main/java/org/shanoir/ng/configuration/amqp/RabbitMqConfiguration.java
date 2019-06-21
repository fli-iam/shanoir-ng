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

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration.
 * 
 * @author msimon
 *
 */
@Configuration
public class RabbitMqConfiguration {

	private static final String TEMPLATE_QUEUE_NAME_IN = "template_queue";
	private static final String TEMPLATE_QUEUE_NAME_OUT = "template_queue_from_ng";

    @Bean
    public static Queue queueIn() {
        return new Queue(TEMPLATE_QUEUE_NAME_IN, true);
    }

    @Bean
    public static Queue queueOut() {
    	return new Queue(TEMPLATE_QUEUE_NAME_OUT, true);
    }

    @Bean
    SimpleMessageListenerContainer container(final ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
    	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TEMPLATE_QUEUE_NAME_IN);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    RabbitMqReceiver receiver() {
        return new RabbitMqReceiver();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(final RabbitMqReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }


}
