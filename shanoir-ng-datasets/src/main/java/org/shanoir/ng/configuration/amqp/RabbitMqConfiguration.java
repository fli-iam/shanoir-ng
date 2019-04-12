package org.shanoir.ng.configuration.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
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

	private static final String DATASET_QUEUE_NAME_OUT = "dataset_queue_from_ng";
	private static final String EXAMINATION_QUEUE_NAME_IN = "examination_queue";
	private static final String EXAMINATION_QUEUE_NAME_OUT = "examination_queue_from_ng";
	private static final String STUDYCARD_QUEUE_TO_STUDY = "studycard_queue_to_study";

    @Bean
    public static Queue datasetQueueOut() {
    	return new Queue(DATASET_QUEUE_NAME_OUT, true);
    }

    @Bean
    public static Queue examinationQueueIn() {
        return new Queue(EXAMINATION_QUEUE_NAME_IN, true);
    }

    @Bean
    public static Queue examinationQueueOut() {
    	return new Queue(EXAMINATION_QUEUE_NAME_OUT, true);
    }

	@Bean
	public static Queue queueToStudy() {
		return new Queue(STUDYCARD_QUEUE_TO_STUDY, true);
	}

    @Bean
    SimpleMessageListenerContainer container(final ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
    	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(EXAMINATION_QUEUE_NAME_IN);
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
