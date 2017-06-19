package org.shanoir.ng.configuration.amqp;

import org.springframework.amqp.core.Queue;
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

	private static final String ACQ_EQPT_QUEUE_NAME_OUT = "acq_eqpt_queue_from_ng";
	private static final String CENTER_QUEUE_NAME_OUT = "center_queue_from_ng";
	private static final String MANUFACTURER_MODEL_QUEUE_NAME_OUT = "manufacturer_model_queue_from_ng";
	private static final String MANUFACTURER_QUEUE_NAME_OUT = "manufacturer_queue_from_ng";
	private static final String STUDY_QUEUE_NAME_IN = "study_queue_to_ng";
	private static final String STUDY_DELETE_QUEUE_NAME_IN = "study_delete_queue_to_ng";
	private static final String STUDY_QUEUE_NAME_OUT = "study_queue_from_ng";
	private static final String SUBJECT_RPC_QUEUE_OUT = "subject_queue_with_RPC_from_ng";
	private static final String SUBJECT_RPC_QUEUE_IN = "subject_queue_with_RPC_to_ng";
	private static final String SUBJECT_QUEUE_OUT = "subject_queue_from_ng";

	// Queue from MS studycard
	private static final String STUDYCARD_QUEUE_TO_STUDY = "studycard_queue_to_study";

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
	public static Queue centerQueueOut() {
		return new Queue(CENTER_QUEUE_NAME_OUT, true);
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
	public static Queue studyQueueIn() {
		return new Queue(STUDY_QUEUE_NAME_IN, true);
	}

	@Bean
	public static Queue studyQueueDeleteIn() {
		return new Queue(STUDY_DELETE_QUEUE_NAME_IN, true);
	}

	@Bean
	public static Queue studyQueueOut() {
		return new Queue(STUDY_QUEUE_NAME_OUT, true);
	}

	@Bean
	public static Queue subjectRPCQueueOut() {
		return new Queue(SUBJECT_RPC_QUEUE_OUT, true);
	}

	@Bean
	public static Queue subjectRPCQueueIn() {
		return new Queue(SUBJECT_RPC_QUEUE_IN, true);
	}

	@Bean
	public static Queue subjectQueueOut() {
		return new Queue(SUBJECT_QUEUE_OUT, true);
	}

	@Bean
	public static Queue studycardQueue() {
		return new Queue(STUDYCARD_QUEUE_TO_STUDY, true);
	}

}
