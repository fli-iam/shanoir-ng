package org.shanoir.ng.shared.configuration;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Centralized configuration for RabbitMQ.
 * Declares:
 * - all queues
 * - all exchanges
 * @author JComeD
 *
 */
@Configuration
@Profile("!test")
public class RabbitMQConfiguration {

	////////////////// QUEUES //////////////////

	public static final String DELETE_USER_QUEUE = "delete-user-queue";

	public static final String STUDY_USER_QUEUE_DATASET = "study-user-queue-dataset";

	public static final String MS_USERS_TO_MS_STUDIES_USER_DELETE = "ms_users_to_ms_studies_user_delete";

	public static final String SHANOIR_EVENTS_QUEUE = "shanoir_events_queue";

	public static final String STUDY_USER_QUEUE_IMPORT = "study-user-queue-import";

	public static final String STUDY_USER_QUEUE = "study-user";

	private static final String ACQ_EQPT_QUEUE_NAME_OUT = "acq_eqpt_queue_from_ng";

	private static final String CENTER_QUEUE_NAME_OUT = "center_queue_from_ng";

	private static final String COIL_QUEUE_NAME_OUT = "coil_queue_from_ng";
	
	private static final String DELETE_ACQ_EQPT_QUEUE_NAME_OUT = "delete_acq_eqpt_queue_from_ng";

	private static final String DELETE_CENTER_QUEUE_NAME_OUT = "delete_center_queue_from_ng";

	private static final String DELETE_COIL_QUEUE_NAME_OUT = "delete_coil_queue_from_ng";
	
	private static final String MANUFACTURER_MODEL_QUEUE_NAME_OUT = "manufacturer_model_queue_from_ng";

	private static final String MANUFACTURER_QUEUE_NAME_OUT = "manufacturer_queue_from_ng";

	private static final String STUDY_QUEUE_NAME_IN = "study_queue_to_ng";

	private static final String STUDY_DELETE_QUEUE_NAME_IN = "study_delete_queue_to_ng";

	private static final String STUDY_QUEUE_NAME_OUT = "study_queue_from_ng";

	private static final String SUBJECT_RPC_QUEUE_OUT = "subject_queue_with_RPC_from_ng";

	private static final String SUBJECT_RPC_QUEUE_IN = "subject_queue_with_RPC_to_ng";

	private static final String SUBJECT_QUEUE_OUT = "subject_queue_from_ng";

	private static final String STUDYCARD_QUEUE_TO_STUDY = "studycard_queue_to_study";

	public static final String SUBJECTS_QUEUE = "subjects-queue";

	////////////////// EXCHANGES //////////////////

	public static final String EVENTS_EXCHANGE = "events-exchange";

	public static final String STUDY_USER_EXCHANGE = "study-user-exchange";

	public static final String SUBJECTS_EXCHANGE = "subjects-exchange";


    @Bean
    public static Queue getMSUsersToMSStudiesUserDelete() {
    		return new Queue(MS_USERS_TO_MS_STUDIES_USER_DELETE, true);
    }

    @Bean
    public static Queue getShanoirEventsQueue() {
    	return new Queue(SHANOIR_EVENTS_QUEUE, true);
    }

	@Bean
	public static Queue studyUserQueueImport() {
		return new Queue(STUDY_USER_QUEUE_IMPORT, true);
	}
	
	@Bean
	public static Queue studyUserDatasetQueue() {
		return new Queue(STUDY_USER_QUEUE_DATASET, true);
	}

	@Bean
	public static Queue deleteUserQueue() {
		return new Queue(DELETE_USER_QUEUE, true);
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
	public static Queue coilQueueOut() {
		return new Queue(COIL_QUEUE_NAME_OUT, true);
	}

	@Bean
	public static Queue deleteAcqEqptQueueOut() {
		return new Queue(DELETE_ACQ_EQPT_QUEUE_NAME_OUT, true);
	}

	@Bean
	public static Queue deleteCenterQueueOut() {
		return new Queue(DELETE_CENTER_QUEUE_NAME_OUT, true);
	}
	
	@Bean
	public static Queue deleteCoilQueueOut() {
		return new Queue(DELETE_COIL_QUEUE_NAME_OUT, true);
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
	
	@Bean
	public static Queue studyUserQueue() {
		return new Queue(STUDY_USER_QUEUE, true);
	}

	@Bean
	public static Queue subjectsQueue() {
		return new Queue(SUBJECTS_QUEUE, true);
	}

	@Bean
	public FanoutExchange fanout() {
	    return new FanoutExchange(STUDY_USER_EXCHANGE, true, false);
	}

	@Bean
	public TopicExchange topicExchange() {
	    return new TopicExchange(EVENTS_EXCHANGE);
	}

	@Bean
	public FanoutExchange fanoutSubjectExchange() {
	    return new FanoutExchange(STUDY_USER_EXCHANGE, true, false);
	}
}
