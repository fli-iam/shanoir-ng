package org.shanoir.ng.messaging;

import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.study.rights.command.StudyUserCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StudyUserUpdateBroadcastService {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyUserUpdateBroadcastService.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	
	public void broadcast(Iterable<StudyUserCommand> commands) throws MicroServiceCommunicationException {
		try {
			String str = new ObjectMapper().writeValueAsString(commands);
			rabbitTemplate.convertAndSend("study-user-exchange", "study-user", str);
			
			LOG.debug("Brodcasted study-user changes : " + str);
			
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Could not send data to study-user-exchange");
		}
	}

}
