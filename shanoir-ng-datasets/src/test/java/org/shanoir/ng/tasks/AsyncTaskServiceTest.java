package org.shanoir.ng.tasks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test class for AsyncTaskService.
 * Testing if the task is well sent to users MS everytime
 * @author fli
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AsyncTaskServiceTest {

	@Autowired
	public AsyncTaskService service;

	@MockBean
	RabbitTemplate template;

	@Test
	public void testAddTask() {
		// GIVEN a new task to add
		AsyncTask t = new AsyncTask("label", 16813L);

		// WHEN we add the task
		service.addTask(t);

		// THEN the task is sent using RabbitMQ
		ArgumentCaptor<String> argumentCatcher = new ArgumentCaptor();
		Mockito.verify(template).convertAndSend(Mockito.eq("datasets-user-exchange"), Mockito.eq(""), argumentCatcher.capture());
		String message = argumentCatcher.getValue();
		assertNotNull(message);
		assertTrue(message.contains(t.getLabel()));
		assertTrue(message.contains(t.getId()));
		assertTrue(message.contains(t.getMessage()));
		assertTrue(message.contains("" + t.getProgress()));
		assertTrue(message.contains("" + t.getUser()));
	}
}
