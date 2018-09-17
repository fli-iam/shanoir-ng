package org.shanoir.ng.configuration.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitMQConfiguration {

	private static final String MS_USERS_TO_MS_STUDIES_USER_DELETE = "ms_users_to_ms_studies_user_delete";

    @Bean
    public static Queue getMSUsersToMSStudiesUserDelete() {
    		return new Queue(MS_USERS_TO_MS_STUDIES_USER_DELETE, true);
    }

}
