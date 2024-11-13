package org.shanoir.ng;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ActiveProfiles("test")
public class TestConfiguration {

	@MockBean
	private RabbitTemplate rabbitTemplate;

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
}