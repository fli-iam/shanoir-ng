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

	private final static String ACQ_EQPT_QUEUE_NAME_OUT = "acq_eqpt_queue_from_ng";
	private final static String MANUFACTURER_MODEL_QUEUE_NAME_OUT = "manufacturer_model_queue_from_ng";
	private final static String MANUFACTURER_QUEUE_NAME_OUT = "manufacturer_queue_from_ng";
	private final static String STUDY_QUEUE_NAME_IN = "study_queue";
	private final static String STUDY_QUEUE_NAME_OUT = "study_queue_from_ng";

    @Bean
    public static Queue acqEqptQueueOut() {
    	return new Queue(ACQ_EQPT_QUEUE_NAME_OUT, true);
    }

    @Bean
    public static Queue manufacturerModelQueueOut() {
    	return new Queue(MANUFACTURER_MODEL_QUEUE_NAME_OUT, true);
    }
    
    @Bean
    public static Queue manufacturerQueueOut() {
    	return new Queue(MANUFACTURER_QUEUE_NAME_OUT, true);
    }

    @Bean
    public static Queue queueIn() {
    	return new Queue(STUDY_QUEUE_NAME_IN, true);
    }
    
    @Bean
    public static Queue queueOut() {
    	return new Queue(STUDY_QUEUE_NAME_OUT, true);
    }
    
    @Bean
    SimpleMessageListenerContainer container(final ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
    	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(STUDY_QUEUE_NAME_IN);
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
