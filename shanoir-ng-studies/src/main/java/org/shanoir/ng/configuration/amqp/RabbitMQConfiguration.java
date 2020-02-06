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

import org.springframework.amqp.core.FanoutExchange;
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
	private static final String STUDY_NAME_UPDATE = "study_name_update";
	private static final String SUBJECT_NAME_UPDATE = "subject_name_update";

    
//	@Bean
//	RabbitMqReceiver receiver() {
//		return new RabbitMqReceiver();
//	}

    @Bean
    public static Queue getMSUsersToMSStudiesUserDelete() {
            return new Queue(MS_USERS_TO_MS_STUDIES_USER_DELETE, true);
    }
	
	@Bean
	public static Queue studyUserQueue() {
		return new Queue("study-user", true);
	}
	
	@Bean
    public FanoutExchange fanout() {
        return new FanoutExchange("study-user-exchange", true, false);
    }
	
	@Bean
	public static Queue studyNameUpdateQueue() {
		return new Queue(STUDY_NAME_UPDATE, true);
	}
	
	@Bean
	public static Queue subjectNameUpdateQueue() {
		return new Queue(SUBJECT_NAME_UPDATE, true);
	}
}
