package org.shanoir.ng.tasks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
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
public class ShanoirEventServiceTest {

	@Autowired
	public ShanoirEventService service;

	@MockBean
	RabbitTemplate template;

	@Test
	public void testAddTask() {
		// GIVEN a new task to add
		ShanoirEvent t = new ShanoirEvent();
		t.setId(Long.valueOf(123));
		t.setUserId(Long.valueOf(456));
		t.setMessage("uio");

		// WHEN we add the task
		service.publishEvent(t);

		// THEN the task is sent using RabbitMQ
		ArgumentCaptor<String> argumentCatcher = new ArgumentCaptor();
		Mockito.verify(template).convertAndSend(Mockito.eq("shanoir-events-exchange"), Mockito.eq(""), argumentCatcher.capture());
		String message = argumentCatcher.getValue();
		assertNotNull(message);
		assertTrue(message.contains(t.getId().toString()));
		assertTrue(message.contains(t.getMessage()));
		assertTrue(message.contains("" + t.getUserId()));
	}
}
