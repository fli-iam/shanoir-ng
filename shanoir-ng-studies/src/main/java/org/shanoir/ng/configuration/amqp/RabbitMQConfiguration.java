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

import org.shanoir.ng.messaging.InterMicroservicesCommunicator;
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
public class RabbitMQConfiguration {
	
	private static final String MS_USERS_TO_MS_STUDIES_USER_DELETE = "ms_users_to_ms_studies_user_delete";

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

	// Queue from MS studycard
	private static final String STUDYCARD_QUEUE_TO_STUDY = "studycard_queue_to_study";

    
	@Bean
	InterMicroservicesCommunicator receiver() {
		return new InterMicroservicesCommunicator();
	}

    @Bean
    public static Queue getMSUsersToMSStudiesUserDelete() {
            return new Queue(MS_USERS_TO_MS_STUDIES_USER_DELETE, true);
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

}
