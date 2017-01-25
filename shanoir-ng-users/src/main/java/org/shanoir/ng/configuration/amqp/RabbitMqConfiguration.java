package org.shanoir.ng.configuration.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

	private final static String USER_QUEUE_NAME_IN = "user_queue";
	private final static String USER_QUEUE_NAME_OUT = "user_queue_from_ng";

    @Bean
    public static Queue queueIn() {
        return new Queue(USER_QUEUE_NAME_IN, true);
    }

    @Bean
    public static Queue queueOut() {
    	return new Queue(USER_QUEUE_NAME_OUT, true);
    }

    @Bean
    SimpleMessageListenerContainer container(final ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
    	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(USER_QUEUE_NAME_IN);
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
