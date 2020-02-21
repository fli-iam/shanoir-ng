package org.shanoir.ng.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Service managing asynchroneous tasks as dataset import.
 * Manages a Task cache
 * @author JCome
 *
 */
@Service
public class AsyncTaskService {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private static final Logger LOG = LoggerFactory.getLogger(AsyncTaskService.class);

	/**
	 * Adds a task to a user's list of tasks by broadcasting it to rabbit exchange queue
	 * @param task the task to add
	 */
	public void addTask(AsyncTask task) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			String str = mapper.writeValueAsString(task);
			rabbitTemplate.convertAndSend("datasets-user-exchange", "", str);
		} catch (AmqpException | JsonProcessingException e) {
			LOG.error("The task was not successfuly created: {}", e);
		}
	}

}
