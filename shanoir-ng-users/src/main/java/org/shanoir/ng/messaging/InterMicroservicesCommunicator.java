package org.shanoir.ng.messaging;

import org.shanoir.ng.configuration.amqp.RabbitMQConfiguration;
import org.shanoir.ng.events.UserDeleteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This class handles on using Spring Application Events
 * all communications with other microservices within Shanoir-NG.
 * 
 * @author mkain
 *
 */
@Component
public class InterMicroservicesCommunicator {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(InterMicroservicesCommunicator.class);
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@EventListener
    public void handleUserDeleteEvent(UserDeleteEvent event) {
		try {
			LOG.debug("Start sending UserDeleteEvent...");
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.getMSUsersToMSStudiesUserDelete().getName(), event.getUserId());
			LOG.debug("Sending UserDeleteEvent finished...");
		} catch (AmqpException e) {
			LOG.error("Error while sending message to RabbitMQ", e);
		}
    }
	
}
