package org.shanoir.ng;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Configuration
@ActiveProfiles("test")
public class TestConfiguration {

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

}