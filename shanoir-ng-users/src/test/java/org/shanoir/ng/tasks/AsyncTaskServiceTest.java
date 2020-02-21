package org.shanoir.ng.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test class for AsyncTaskService.
 * Checking tasks cache functionality
 * add, getForUser and periodic remove.
 * @author fli
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AsyncTaskServiceTest {

	private static final Long USER = Long.valueOf(123L);
	private static final Long USER2 = Long.valueOf(1234L);
	private static final Long USER3 = Long.valueOf(12345L);
	private static final Long USER4 = Long.valueOf(123456L);


	AsyncTask task = new AsyncTask("label", USER);

	@Autowired
	AsyncTaskService service;

	@Test
	public void testAddTask() {
		// GIVEN an empty cache
		List<AsyncTask> result = service.getTasks(USER);
		assertTrue(result.isEmpty());

		// WHEN we add a task
		service.addTask(task);
		
		// THEN the task is added to the cache
		result = service.getTasks(USER);
		assertFalse(result.isEmpty());
		assertEquals(task, result.get(0));
		assertEquals("label", task.getLabel());
		
		// UPDATE THE TASK
		task.setLabel("label2");
		service.addTask(task);
		result = service.getTasks(USER);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		assertEquals(task, result.get(0));
		assertEquals("label2", task.getLabel());
	}

	@Test
	public void testGetTasks() {
		// GIVEN an empty cache with multiple cache and users
		List<AsyncTask> result = service.getTasks(USER2);
		assertTrue(result.isEmpty());
		
		AsyncTask task1 = new AsyncTask("labl2", USER2);
		AsyncTask task2 = new AsyncTask("labl3", USER2);
		AsyncTask task3 = new AsyncTask("labael4", USER3);
	
		service.addTask(task1);
		service.addTask(task2);
		service.addTask(task3);

		// WHEN we retrieve them
		result = service.getTasks(USER2);

		// THEN the tasks are retrieved
		assertFalse(result.isEmpty());
		assertEquals(2, result.size());
		assertEquals(task1, result.get(0));
		assertEquals(task2, result.get(1));

		result = service.getTasks(USER3);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		assertEquals(task3, result.get(0));
	}


	@Test
	public void testClearTask() {
		List<AsyncTask> result = service.getTasks(USER4);
		assertTrue(result.isEmpty());
		// GIVEN a cache with some deletable values
		AsyncTask task4 = new AsyncTask("labael5", USER4);
		AsyncTask task5 = new AsyncTask("labael6", USER4);

		task4.endTask();
		task4.setEndDate(LocalDateTime.now().minusDays(8));
		service.addTask(task4);
		service.addTask(task5);

		result = service.getTasks(USER4);
		assertFalse(result.isEmpty());
		assertEquals(2, result.size());

		// WHEN the scheduled clearcache method runs
		service.clearTasks();
		
		// THEN the deletable tasks are deleted from the cache
		// Only task5 is still here
		result = service.getTasks(USER4);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		assertEquals(task5, result.get(0));
	}
}
