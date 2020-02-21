package org.shanoir.ng.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class AsyncTaskTest {

	@Test
	public void testTaskStates() {
		// CREATION
		AsyncTask task = new AsyncTask("label", 656541L);
		assertNotNull(task);
		assertEquals("label", task.getLabel());
		assertEquals(0, task.getProgress(), 0.0002);
		assertNull(task.getEndDate());
		assertNotNull(task.getStartDate());
		assertEquals("Just created", task.getMessage());
		assertNotNull(task.getId());
		String id = task.getId();
		LocalDateTime startD = task.getStartDate();

		// UPDATE
		task.updateTask(0.3f, "update");
		assertNotNull(task);
		assertEquals("label", task.getLabel());
		assertEquals(startD, task.getStartDate());
		assertEquals(0.3f, task.getProgress(), 0.0002);
		assertNull(task.getEndDate());
		assertEquals("update", task.getMessage());
		assertEquals(id, task.getId());
		assertFalse(task.taskInError());

		// FAIL
		task.failTask("ERROR");
		assertNotNull(task);
		assertEquals("label", task.getLabel());
		assertEquals(startD, task.getStartDate());
		assertEquals(-1f, task.getProgress(), 0.0002);
		assertNotNull(task.getEndDate());
		assertEquals("ERROR", task.getMessage());
		assertEquals(id, task.getId());
		assertTrue(task.taskInError());

		// END
		task.endTask();
		assertNotNull(task);
		assertEquals("label", task.getLabel());
		assertEquals(startD, task.getStartDate());
		assertEquals(1f, task.getProgress(), 0.0002);
		assertNotNull(task.getEndDate());
		assertEquals("Success", task.getMessage());
		assertEquals(id, task.getId());
		
		assertTrue(task.taskFinished());
		
		// DELETABLE
		assertFalse(task.deletable());
		
		task.setEndDate(LocalDateTime.now().minusDays(8));
		assertTrue(task.deletable());

		// in error due to deletable
		task.updateTask(0.3f, "retest");
		task.setStartDate(LocalDateTime.now().minusDays(8));
		// Not deletable anymore because in error
		assertFalse(task.deletable());
		assertTrue(task.taskInError());

		assertEquals("label", task.getLabel());
		assertEquals(-1f, task.getProgress(), 0.0002);
		assertNotNull(task.getEndDate());
		assertEquals("This task was set in error due to a too long treatment: more than 7 days.", task.getMessage());
		assertEquals(id, task.getId());
	}

	@Test
	public void testPourcentage() {
		// Just a test to calculate a percentage
		List<List<String>> grandMother = new ArrayList<List<String>>();
		List<String> mother = new ArrayList<>();
		mother.add("enf1");
		mother.add("enf2");
		mother.add("enf3");
		mother.add("enf4");
		mother.add("enf5");
		mother.add("enf6");

		List<String> aunt = new ArrayList<>();
		for (int i = 0 ; i < 10 ; i++) {
			aunt.add("nefew" + i);
		}
		grandMother.add(mother);
		grandMother.add(aunt);

		float pourcentage = 0;
		
		for(List<String> child : grandMother) {
			float parentPourcentage = 1 / grandMother.size();
			for(String children : child) {
				pourcentage += parentPourcentage / child.size();
				System.out.println("child: " + children + " pourcentage: " + pourcentage);
			}
		}
	}
}
