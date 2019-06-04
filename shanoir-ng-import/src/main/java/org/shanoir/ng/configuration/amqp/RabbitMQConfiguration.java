package org.shanoir.ng.configuration.amqp;

import org.shanoir.ng.study.rights.ampq.StudyUserListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

private static final Logger LOG = LoggerFactory.getLogger(RabbitMQConfiguration.class);
	
	@Autowired
	private StudyUserListener listener;
	
	@Bean
	public static org.springframework.amqp.core.Queue studyUserQueue() {
		return new org.springframework.amqp.core.Queue("study-user-queue-import", true);
	}
	
	@Bean
	public FanoutExchange fanout() {
	    return new FanoutExchange("study-user-exchange", true, false);
	}	
	
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue(value = "study-user-queue-import", durable = "true"),
	        exchange = @Exchange(value = "study-user-exchange", ignoreDeclarationExceptions = "true", 
	        	autoDelete = "false", durable = "true", type=ExchangeTypes.FANOUT))
	)
	public void receiveMessage(String commandArrStr) throws AmqpRejectAndDontRequeueException  {
		listener.receiveMessageImport(commandArrStr);
	}

}
