package org.shanoir.ng.messaging;

import org.shanoir.ng.configuration.amqp.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.study.rights.StudyUser;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StudyUserUpdateBroadcastService {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	
	public void broadcastDelete(Long... ids) throws MicroServiceCommunicationException {
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.studyUserDeleteQueue().getName(),
					new ObjectMapper().writeValueAsString(ids));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Serializing an array of Longs should not fail ! ", e);
		} catch (AmqpException e) {
			throw new MicroServiceCommunicationException("Could not send data to queue " 
					+ RabbitMQConfiguration.studyUserDeleteQueue().getName(), e);
		}
	}
	
	public void broadcastCreate(StudyUser... studyUsers) throws MicroServiceCommunicationException {
		for (StudyUser su: studyUsers) if (su.getId() == null) 
			throw new IllegalArgumentException("An create studyUser should have an id");
		
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.studyUserCreateQueue().getName(),
					new ObjectMapper().writeValueAsString(studyUsers));
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Could not send data to queue " 
					+ RabbitMQConfiguration.studyUserCreateQueue().getName(), e);
		}
		
	}
	
	public void broadcastUpdate(StudyUser... studyUsers) throws MicroServiceCommunicationException {
		for (StudyUser su: studyUsers) if (su.getId() == null) 
			throw new IllegalArgumentException("An updated studyUser should have an id");
		
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.studyUserUpdateQueue().getName(),
					new ObjectMapper().writeValueAsString(studyUsers));
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Could not send data to queue " 
					+ RabbitMQConfiguration.studyUserUpdateQueue().getName(), e);
		}
		
	}
	
}
