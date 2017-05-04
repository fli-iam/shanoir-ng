package org.shanoir.ng.configuration.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration.
 *
 * @author msimon
 *
 */
@Configuration
public class RabbitMqConfiguration {

	private final static String STUDYCARD_QUEUE_NAME_IN = "studycard_queue_to_ng";
	private final static String STUDYCARD_DELETE_QUEUE_NAME_IN = "studycard_delete_queue_to_ng";
	private final static String STUDYCARD_QUEUE_NAME_OUT = "studycard_queue_from_ng";

    @Bean
    public static Queue queueIn() {
        return new Queue(STUDYCARD_QUEUE_NAME_IN, true);
    }

    @Bean
    public static Queue queueOut() {
    	return new Queue(STUDYCARD_QUEUE_NAME_OUT, true);
    }

		@Bean
		public static Queue queueDeleteIn() {
			return new Queue(STUDYCARD_DELETE_QUEUE_NAME_IN, true);
		}


		@Bean
    SimpleMessageListenerContainer studyContainer(final ConnectionFactory connectionFactory,
            @Qualifier("studycardListenerAdapter") MessageListenerAdapter listenerAdapter) {
    	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(STUDYCARD_QUEUE_NAME_IN);
        container.setMessageListener(listenerAdapter);
        return container;
    }

		@Bean
    SimpleMessageListenerContainer studycardDeleteContainer(final ConnectionFactory connectionFactory,
            @Qualifier("studycardDeleteListenerAdapter") MessageListenerAdapter listenerAdapter) {
    	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(STUDYCARD_DELETE_QUEUE_NAME_IN);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    RabbitMqReceiver receiver() {
        return new RabbitMqReceiver();
    }

    @Bean
    MessageListenerAdapter studycardListenerAdapter(final RabbitMqReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveStudyCardMessage");
    }


		@Bean
    MessageListenerAdapter studycardDeleteListenerAdapter(final RabbitMqReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveStudyCardDeleteMessage");
    }


}
