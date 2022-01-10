package org.shanoir.ng;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ActiveProfiles("test")
public class TestConfiguration {

	@MockBean
	private RabbitTemplate rabbitTemplate;

}
