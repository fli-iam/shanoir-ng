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

	/** Queue used to import datasets IMPORT => DATASET. */
	public static final String IMPORTER_QUEUE_DATASET = "importer-queue-dataset";

	/** User delete event to notify to studies.  To be overriden by an event ?*/
	public static final String MS_USERS_TO_MS_STUDIES_USER_DELETE = "ms_users_to_ms_studies_user_delete";

	/** Queue for all shanoir events. */
	public static final String SHANOIR_EVENTS_QUEUE = "shanoir-events-queue";

	/** Specific queue for import dataset events. */
	public static final String SHANOIR_EVENTS_QUEUE_IMPORT = "shanoir-events-queue-import";
	
	/** Update / create a study user to dataset MS. */
	public static final String STUDY_USER_QUEUE_DATASET = "study-user-queue-dataset";

	/** Update / create a study user to import MS. */
	public static final String STUDY_USER_QUEUE_IMPORT = "study-user-queue-import";

	/** Queue to notify when a user / study is update / deleted. */
	public static final String STUDY_USER_QUEUE = "study-user";

	/** BIDS purpose => Get a list of subjects to create bids participants file. */
	public static final String SUBJECTS_QUEUE = "subjects-queue";

	/** Study name updated => notify dataset MS to change database. */
	public static final String STUDY_NAME_UPDATE_QUEUE = "study-name-update-queue";

	/** Subject name updated => notify dataset MS to change database. */
	public static final String SUBJECT_NAME_UPDATE_QUEUE = "subject-name-update-queue";
	
	/** Center name updated => notify dataset MS to change database. */
	public static final String CENTER_NAME_UPDATE_QUEUE = "center-name-update-queue";
	
	/** Get the list of subjects for a given study. */
	public static final String DATASET_SUBJECT_QUEUE = "dataset-subjects-queue";
	
	/** Create a subject study for a given subject and study. */
	public static final String DATASET_SUBJECT_STUDY_QUEUE = "dataset-subject-study-queue";

	/** Delete subject => Delete associated examination / datasets. */
	public static final String DELETE_SUBJECT_QUEUE = "delete-subject-queue";

	/** Study deleted => Delete associated datasets. */
	public static final String DELETE_STUDY_QUEUE = "delete-study-queue";
	
	/** Create DS acquisition => Index datasets in solr. */
	public static final String CREATE_DATASET_ACQUISITION_QUEUE = "create-dataset-acquisition-queue";

	/** Queue to retrieve informations about studyc cards. */
	public static final String FIND_STUDY_CARD_QUEUE = "find-study-card-queue";

	/** Queue to retrieve the center ID from an acquisition equipement ID. */
	public static final String ACQUISITION_EQUIPEMENT_CENTER_QUEUE = "acquisition-equipement-center-queue";
	
	/** Queue to create exam for import bids. */
	public static final String EXAMINATION_CREATION_QUEUE = "examination-creation-queue";

	/** Queue to create a study_user when subscribing to a challenge */
	public static final String CHALLENGE_SUBSCRIPTION_QUEUE = "challenge-subscription-queue";
	
	/** Queue used to get information for study_examination relationship.*/
	public static final String EXAMINATION_STUDY_QUEUE = "examination-study-queue";

	/** Queue used to get information for study_examination deletion relationship.*/
	public static final String EXAMINATION_STUDY_DELETE_QUEUE = "examination-study-delete-queue";

	/** Send a mail from dataset microservice to ms users */
	public static final String IMPORT_DATASET_MAIL_QUEUE = "import-dataset-mail-queue";

	/** Queue to re-convert using a different nifti converter */
	public static final String NIFTI_CONVERSION_QUEUE = "nifti-conversion-queue";

	////////// IN / OUT THINGS (to be comented to make it clearer) /////////
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

	////////////////// EXCHANGES //////////////////

	/** Exchange used to publish / treat all sort of shanoir events. */
	public static final String EVENTS_EXCHANGE = "events-exchange";

	/** Exchange to notify when a user / study is update / deleted. */
	public static final String STUDY_USER_EXCHANGE = "study-user-exchange";

    @Bean
    public static Queue getMSUsersToMSStudiesUserDelete() {
    		return new Queue(MS_USERS_TO_MS_STUDIES_USER_DELETE, true);
    }

    @Bean
    public static Queue getShanoirEventsQueue() {
    	return new Queue(SHANOIR_EVENTS_QUEUE, true);
    }

    @Bean
    public static Queue getShanoirEventsQueueImport() {
    	return new Queue(SHANOIR_EVENTS_QUEUE_IMPORT, true);
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
	public static Queue studyUserQueue() {
		return new Queue(STUDY_USER_QUEUE, true);
	}

	@Bean
	public static Queue subjectsQueue() {
		return new Queue(SUBJECTS_QUEUE, true);
	}
	
	@Bean
	public static Queue datasetSubjectQueue() {
		return new Queue(DATASET_SUBJECT_QUEUE, true);
	}

	@Bean
	public static Queue datasetSubjectStudyQueue() {
		return new Queue(DATASET_SUBJECT_STUDY_QUEUE, true);
	}
	
	@Bean
	public static Queue importerQueue() {
		return new Queue(IMPORTER_QUEUE_DATASET, true);
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


	@Bean
	public static Queue studyNameUpdateQueue() {
		return new Queue(STUDY_NAME_UPDATE_QUEUE, true);
	}
	
	@Bean
	public static Queue subjectNameUpdateQueue() {
		return new Queue(SUBJECT_NAME_UPDATE_QUEUE, true);
	}

	@Bean
	public static Queue centerNameUpdateQueue() {
		return new Queue(CENTER_NAME_UPDATE_QUEUE, true);
	}

	@Bean
	public static Queue createDatasetAcquisitionQueue() {
		return new Queue(CREATE_DATASET_ACQUISITION_QUEUE, true);
	}

	@Bean
	public static Queue findStudyCardQueue() {
		return new Queue(FIND_STUDY_CARD_QUEUE, true);
	}
	
	@Bean
	public static Queue acquisitionEquipementCenterQueue() {
		return new Queue(ACQUISITION_EQUIPEMENT_CENTER_QUEUE, true);
	}
	
	@Bean
	public static Queue examinationCreationQueue() {
		return new Queue(EXAMINATION_CREATION_QUEUE, true);
	}

	@Bean
	public static Queue challengeSubscriptionQueue() {
		return new Queue(CHALLENGE_SUBSCRIPTION_QUEUE, true);
	}

	@Bean
	public static Queue examinationStudyQueue() {
		return new Queue(EXAMINATION_STUDY_QUEUE, true);
	}

	@Bean
	public static Queue examinationStudyDeleteQueue() {
		return new Queue(EXAMINATION_STUDY_DELETE_QUEUE, true);
	}

	@Bean
	public static Queue niftiConversionQueue() {
		return new Queue(NIFTI_CONVERSION_QUEUE, true);
	}
	
	@Bean
	public static Queue importDatasetMail() {
		return new Queue(IMPORT_DATASET_MAIL_QUEUE, true);
	}
}
