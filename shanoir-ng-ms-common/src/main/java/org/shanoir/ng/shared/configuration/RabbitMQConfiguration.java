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
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private ConnectionFactory connectionFactory;

	@Bean(name = "multipleConsumersFactory")
	public SimpleRabbitListenerContainerFactory multipleConsumersFactory() {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMaxConcurrentConsumers(100);
		factory.setConcurrentConsumers(10);
		factory.setStartConsumerMinInterval(100L);
		factory.setConsecutiveActiveTrigger(1);
		factory.setAutoStartup(true);
		factory.setPrefetchCount(1);
		return factory;
	}

	@Bean(name = "singleConsumerFactory")
	public SimpleRabbitListenerContainerFactory singleConsumerFactory() {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setConcurrentConsumers(1);
		return factory;
	}

	////////////////// QUEUES //////////////////

	/** Queue used to import datasets IMPORT => DATASET. */
	public static final String IMPORTER_QUEUE_DATASET = "importer-queue-dataset";

	/** User delete event to notify to studies. To be overriden by an event ?*/
	public static final String MS_USERS_TO_MS_STUDIES_USER_DELETE = "ms_users_to_ms_studies_user_delete";

	/** Queue for all shanoir events. */
	public static final String SHANOIR_EVENTS_QUEUE = "shanoir-events-queue";

	/** Specific queue for import dataset events. */
	public static final String SHANOIR_EVENTS_QUEUE_IMPORT = "shanoir-events-queue-import";
	
	/** Update / create a study user to dataset MS. */
	public static final String STUDY_USER_QUEUE_DATASET = "study-user-queue-dataset";

	/** Update / create a study user to import MS. */
	public static final String STUDY_USER_QUEUE_IMPORT = "study-user-queue-import";

	/** Queue to notify when a user / study is updated / deleted. */
	public static final String STUDY_USER_QUEUE = "study-user";

	/** Update / create a study user to users MS. */
	public static final String STUDY_USER_QUEUE_USERS = "study-user-queue-users";

	/** BIDS purpose => Get a list of subjects to create bids participants file. */
	public static final String SUBJECTS_QUEUE = "subjects-queue";

	/** Preclinical subject creation => Check if a subject with this name already exists **/
	public static final String SUBJECTS_NAME_QUEUE = "subjects-name-queue";

	/** Study name updated => notify dataset MS to change database. */
	public static final String STUDY_NAME_UPDATE_QUEUE = "study-name-update-queue";

	/** Subject name updated => notify dataset MS to change database. */
	public static final String SUBJECT_UPDATE_QUEUE = "subject-update-queue";
	
	/** Center name updated => notify MS Datasets to change database. */
	public static final String CENTER_UPDATE_QUEUE = "center-update-queue";

	/** Center created => notify MS Datasets to change database. */
	public static final String CENTER_CREATE_QUEUE = "center-create-queue";

	/** Center deleted => notify MS Datasets to change database. */
	public static final String CENTER_DELETE_QUEUE = "center-delete-queue";

	public static final String STUDY_CENTER_QUEUE = "study-center-queue";
	
	/** Get the list of subjects for a given study. */
	public static final String DATASET_SUBJECT_QUEUE = "dataset-subjects-queue";

	public static final String COPY_DATASETS_TO_STUDY_QUEUE = "copy-datasets-to-study-queue";
	
	public static final String STUDY_DATASETS_DETAILED_STORAGE_VOLUME = "study-datasets-detailed-storage-volume";

	public static final String STUDY_DATASETS_TOTAL_STORAGE_VOLUME = "study-datasets-total-storage-volume";

	public static final String EXECUTION_MONITORING_TASK = "execution-monitoring-task";

	/** Get the type of dataset from a given study. */
	public static final String STUDY_DATASET_TYPE = "study-dataset-type";
	
	/** Create a subject study for a given subject and study. */
	public static final String DATASET_SUBJECT_STUDY_QUEUE = "dataset-subject-study-queue";
	
	/** Create tags on subject-study via quality control using study cards: ms datasets -> ms studies */
	public static final String STUDIES_SUBJECT_STUDY_STUDY_CARD_TAG = "studies-subject-study-study-card-tag";

	/** Delete subject => Delete associated examination / datasets. */
	public static final String DELETE_SUBJECT_QUEUE = "delete-subject-queue";

	/** Delete animal subject => Delete associated subject. */
	public static final String DELETE_ANIMAL_SUBJECT_QUEUE = "delete-animal-subject-queue";

	/** Delete user queue. */
	public static final String DELETE_USER_QUEUE = "delete-user-queue";
	
	/** Study deleted => Delete associated datasets. */
	public static final String DELETE_STUDY_QUEUE = "delete-study-queue";
	
	/** Queue to retrieve informations about studyc cards. */
	public static final String FIND_STUDY_CARD_QUEUE = "find-study-card-queue";

	public static final String ACQUISITION_EQUIPMENT_CREATE_QUEUE = "acquisition-equipment-create-queue";

	/** Queue to retrieve the center ID from an acquisition equipment ID. */
	public static final String ACQUISITION_EQUIPMENT_CENTER_QUEUE = "acquisition-equipment-center-queue";

	/** Queue to retrieve the center ID from an acquisition equipment ID. */
	public static final String ACQUISITION_EQUIPMENT_UPDATE_QUEUE = "acquisition-equipment-update-queue";

	/** Queue to send dua draft by mail */
	public static final String DUA_DRAFT_MAIL_QUEUE = "dua-draft-mail-queue";
	
	/** Queue to create exam for import bids. */
	public static final String EXAMINATION_CREATION_QUEUE = "examination-creation-queue";

	/** Queue used to get information for study_examination relationship.*/
	public static final String EXAMINATION_STUDY_QUEUE = "examination-study-queue";

	/** Queue used to get information for study_examination deletion relationship.*/
	public static final String EXAMINATION_STUDY_DELETE_QUEUE = "examination-study-delete-queue";

	/** Send a mail from dataset microservice to ms users */
	public static final String IMPORT_DATASET_MAIL_QUEUE = "import-dataset-mail-queue";

	/** Send a mail from dataset microservice to ms users for import dataset failure*/
	public static final String IMPORT_DATASET_FAILED_MAIL_QUEUE = "import-dataset-failed-mail-queue";
	
	/** Send a mail from studies microservice to ms users */
	public static final String STUDY_USER_MAIL_QUEUE = "study-user-mail-queue";

	/** Queue to re-convert using a different nifti converter */
	public static final String NIFTI_CONVERSION_QUEUE = "nifti-conversion-queue";

	/** Queue to consume BIDS related events */
	public static final String BIDS_EVENT_QUEUE = "bids-event-queue";

	/** Queue to consume BIDS related events */
	public static final String RELOAD_BIDS = "reload-bids-queue";
	
	/** Queue to create examination extra data from import */
	public static final String EXAMINATION_EXTRA_DATA_QUEUE = "examination-extra-data-queue";

	/** Queue to create all bids dataset acquisitions */
	public static final String IMPORTER_BIDS_DATASET_QUEUE = "importer-bids-dataset-queue";

	/** Queue to create get equipment ID from code. */
	public static final String ACQUISITION_EQUIPMENT_CODE_QUEUE = "acquisition-equipment-code-queue";

	/** Queue to get the study card from a equipment code. */
	public static final String IMPORT_STUDY_CARD_QUEUE="import-study-card-queue";

	/** Queue to get an equipment id from a code. */
	public static final String EQUIPMENT_FROM_CODE_QUEUE="equipment-from-code-queue";

	/** Queue to create a study_user when subscribing to a study */
	public static final String STUDY_SUBSCRIPTION_QUEUE = "study-subscription-queue";

	/** Queue used to get the list of studies I can Admin. */
	public static final String STUDY_I_CAN_ADMIN_QUEUE = "study-i-can-admin";
	
	/** Queue used to send invitation email for a given study. */
	public static final String STUDY_INVITATION_QUEUE = "study-invitation-queue";
	
	/** Queue used to get the list of study admins. */
	public static final String STUDY_ADMINS_QUEUE = "study-admin-queue";

	/** Queue used to get the name of a study from ID */
	public static final String STUDY_NAME_QUEUE = "study-name-queue";
	
	/** Queue used to import eeg data */
	public static final String IMPORT_EEG_QUEUE = "import-eeg-queue";

	/** Queue used to get anonymisation profile of a study. */
	public static final String STUDY_ANONYMISATION_PROFILE_QUEUE = "study-anonymisation-profile-queue";

    /** Queue used to make bruker to dicom conversion. */
    public static final String BRUKER_CONVERSION_QUEUE = "bruker-conversion-queue";

    /** Queue used to make anima to nifti conversion. */
    public static final String ANIMA_CONVERSION_QUEUE = "anima-conversion-queue";

	////////////////// EXCHANGES //////////////////

	/** Exchange used to publish / treat all sort of shanoir events. */
	public static final String EVENTS_EXCHANGE = "events-exchange";

	/** Exchange to notify when a user / study is updated / deleted. */
	public static final String STUDY_USER_EXCHANGE = "study-user-exchange";

	/** Exchange to notify when a subject / study is updated / deleted. */
	public static final String SUBJECT_STUDY_EXCHANGE = "subject-study-exchange";

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
	public static Queue studyUserQueueUsers() {
		return new Queue(STUDY_USER_QUEUE_USERS, true);
	}

	@Bean
	public static Queue studyUserDatasetQueue() {
		return new Queue(STUDY_USER_QUEUE_DATASET, true);
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
	public static Queue subjectsNameQueue() {
		return new Queue(SUBJECTS_NAME_QUEUE, true);
	}

	@Bean
	public static Queue datasetSubjectQueue() {
		return new Queue(DATASET_SUBJECT_QUEUE, true);
	}
	@Bean
	public static Queue copyDatasetToStudyQueue() {
		return new Queue(COPY_DATASETS_TO_STUDY_QUEUE, true);
	}

	@Bean
	public static Queue studyDatasetsDetailedStorageVolumeQueue() {
		return new Queue(STUDY_DATASETS_DETAILED_STORAGE_VOLUME, true);
	}

	@Bean
	public static Queue studyDatasetsTotalStorageVolumeQueue() {
		return new Queue(STUDY_DATASETS_TOTAL_STORAGE_VOLUME, true);
	}

	@Bean
	public static Queue sexecutionMonitoringEventQueue() {
		return new Queue(EXECUTION_MONITORING_TASK, true);
	}

	@Bean
	public static Queue studyDatasetTypeQueue() { return new Queue(STUDY_DATASET_TYPE, true); }

	@Bean
	public static Queue datasetSubjectStudyQueue() {
		return new Queue(DATASET_SUBJECT_STUDY_QUEUE, true);
	}
	
	@Bean
	public static Queue studiesSubjectStudyStudyCardTagQueue() {
		return new Queue(STUDIES_SUBJECT_STUDY_STUDY_CARD_TAG, true);
	}

	@Bean
	public static Queue deleteSubjectQueue() {
		return new Queue(DELETE_SUBJECT_QUEUE, true);
  	}
	
  	@Bean
	public static Queue deleteAnimalSubjectQueue() {
		return new Queue(DELETE_ANIMAL_SUBJECT_QUEUE, true);
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
	public static Queue subjectUpdateQueue() {
		return new Queue(SUBJECT_UPDATE_QUEUE, true);
	}

	@Bean
	public static Queue centerUpdateQueue() {
		return new Queue(CENTER_UPDATE_QUEUE, true);
	}

	@Bean
	public static Queue centerCreateQueue() {
		return new Queue(CENTER_CREATE_QUEUE, true);
	}

	@Bean
	public static Queue centerDeleteQueue() {
		return new Queue(CENTER_DELETE_QUEUE, true);
	}

	@Bean
	public static Queue findStudyCardQueue() {
		return new Queue(FIND_STUDY_CARD_QUEUE, true);
	}

	@Bean
	public static Queue acquisitionEquipmentCreateQueue() {
		return new Queue(ACQUISITION_EQUIPMENT_CENTER_QUEUE, true);
	}

	@Bean
	public static Queue acquisitionEquipmentCenterQueue() {
		return new Queue(ACQUISITION_EQUIPMENT_CENTER_QUEUE, true);
	}

	@Bean
	public static Queue acquisitionEquipmentUpdateQueue() {
		return new Queue(ACQUISITION_EQUIPMENT_UPDATE_QUEUE, true);
	}

	@Bean
	public static Queue duaDraftMailQueue() {
		return new Queue(DUA_DRAFT_MAIL_QUEUE, true);
	}

	@Bean
	public static Queue examinationCreationQueue() {
		return new Queue(EXAMINATION_CREATION_QUEUE, true);
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
	public static Queue importDatasetMailQueue() {
		return new Queue(IMPORT_DATASET_MAIL_QUEUE, true);
	}

	@Bean
	public static Queue bidsEventQueue() {
		return new Queue(BIDS_EVENT_QUEUE, true);
	}

	@Bean
	public static Queue reloadBidsQueue() {
		return new Queue(RELOAD_BIDS, true);
	}

	@Bean
	public static Queue examinationExtraDataQueue() {
		return new Queue(EXAMINATION_EXTRA_DATA_QUEUE, true);
	}

	@Bean
	public static Queue importDatasetFailedMailQueue() {
		return new Queue(IMPORT_DATASET_FAILED_MAIL_QUEUE, true);
	}
	
	@Bean
	public static Queue studyUserMailQueue() {
		return new Queue(STUDY_USER_MAIL_QUEUE, true);
	}
	
	@Bean
	public static Queue importBidsDatasetQueue() {
		return new Queue(IMPORTER_BIDS_DATASET_QUEUE, true);
	}

	@Bean
	public static Queue acquisitionEquipmentCodeQueue() {
		return new Queue(ACQUISITION_EQUIPMENT_CODE_QUEUE, true);
	}

	@Bean
	public static Queue equipmentFromCodeQueue() {
		return new Queue(EQUIPMENT_FROM_CODE_QUEUE, true);
	}

	@Bean
	public static Queue deleteUserQueue() {
		return new Queue(DELETE_USER_QUEUE, true);
	}

	@Bean
	public static Queue studySubscriptionQueue() {
		return new Queue(STUDY_SUBSCRIPTION_QUEUE, true);
	}
	
	@Bean
	public static Queue studyICanAdminQueue() {
		return new Queue(STUDY_I_CAN_ADMIN_QUEUE, true);
	}

	@Bean
	public static Queue studyInvitationQueue() {
		return new Queue(STUDY_INVITATION_QUEUE, true);
	}

	@Bean
	public static Queue importStudyCardQueue() {
		return new Queue(IMPORT_STUDY_CARD_QUEUE, true);
	}

	@Bean
	public static Queue studyAdminQueue() {
		return new Queue(STUDY_ADMINS_QUEUE, true);
	}

	@Bean
	public static Queue studyNameQueue() {
		return new Queue(STUDY_NAME_QUEUE, true);
	}

	@Bean
	public static Queue importEEGQueue() {
		return new Queue(IMPORT_EEG_QUEUE, true);
	}
	
	@Bean
	public static Queue studyAnonymisationProfileQueue() {
		return new Queue(STUDY_ANONYMISATION_PROFILE_QUEUE, true);
	}

	@Bean
	public static Queue brukerConversionQueue() {
		return new Queue(BRUKER_CONVERSION_QUEUE, true);
	}

	@Bean
	public static Queue animaConversionQueue() {
		return new Queue(ANIMA_CONVERSION_QUEUE, true);
	}


}
