package org.shanoir.ng.configuration.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.DirectExchange;

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
		private final static String STUDY_QUEUE_NAME_IN = "study_queue_to_ng";
		private final static String STUDY_DELETE_QUEUE_NAME_IN = "study_delete_queue_to_ng";
		private final static String STUDY_QUEUE_NAME_OUT = "study_queue_from_ng";
		private final static String SUBJECT_RPC_QUEUE_OUT = "subject_queue_with_RPC_from_ng";
		private final static String SUBJECT_RPC_QUEUE_IN = "subject_queue_with_RPC_to_ng";
		private final static String SUBJECT_QUEUE_OUT = "subject_queue_from_ng";


		@Bean
		public RabbitMqRPCClient client() {
				return new RabbitMqRPCClient();
		}

		@Bean
		RabbitMqReceiver receiver() {
				return new RabbitMqReceiver();
		}

		@Bean
		RabbitMqRPCReceiver receiverRPC() {
				return new RabbitMqRPCReceiver();
		}

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
    public static Queue queueDeleteIn() {
    	return new Queue(STUDY_DELETE_QUEUE_NAME_IN, true);
    }

    @Bean
    public static Queue queueOut() {
    	return new Queue(STUDY_QUEUE_NAME_OUT, true);
    }

		@Bean
		public static Queue subjectRPCQueueOut() {
			return new Queue(SUBJECT_RPC_QUEUE_OUT, true);
		}

		@Bean
		public static Queue subjectRPCQueueIn(){
			return new Queue(SUBJECT_RPC_QUEUE_IN, true);
		}

		@Bean
		public static Queue subjectQueueOut() {
			return new Queue(SUBJECT_QUEUE_OUT, true);
		}

    // @Bean
    // SimpleMessageListenerContainer studyContainer(final ConnectionFactory connectionFactory,
    //         @Qualifier("studyListenerAdapter") MessageListenerAdapter listenerAdapter) {
    // 	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory);
    //     container.setQueueNames(STUDY_QUEUE_NAME_IN);
    //     container.setMessageListener(listenerAdapter);
    //     return container;
    // }


		// @Bean
    // SimpleMessageListenerContainer studyDeleteContainer(final ConnectionFactory connectionFactory,
    //         @Qualifier("studyDeleteListenerAdapter") MessageListenerAdapter listenerAdapter) {
    // 	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory);
    //     container.setQueueNames(STUDY_DELETE_QUEUE_NAME_IN);
    //     container.setMessageListener(listenerAdapter);
    //     return container;
    // }

		// @Bean
    // SimpleMessageListenerContainer subjectRpcContainer(final ConnectionFactory connectionFactory,
    //         @Qualifier("rpcListenerAdapter") MessageListenerAdapter listenerAdapter) {
    // 	final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    //     container.setConnectionFactory(connectionFactory);
    //     container.setQueueNames(SUBJECT_RPC_QUEUE_IN);
		// 		//container.setReplyTo()
    //     container.setMessageListener(listenerAdapter);
    //     return container;
    // }



    // @Bean
    // MessageListenerAdapter studyListenerAdapter(final RabbitMqReceiver receiver) {
    //     return new MessageListenerAdapter(receiver, "receiveStudyMessage");
    // }
		//
		// @Bean
    // MessageListenerAdapter studyDeleteListenerAdapter(final RabbitMqReceiver receiver) {
    //     return new MessageListenerAdapter(receiver, "receiveStudyDeleteMessage");
    // }
		//
		//
		// @Bean
    // MessageListenerAdapter rpcListenerAdapter(final RabbitMqRPCReceiver receiver) {
    //     return new MessageListenerAdapter(receiver, "receiveMessage");
    // }

}
