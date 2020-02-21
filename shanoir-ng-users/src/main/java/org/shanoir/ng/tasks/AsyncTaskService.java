package org.shanoir.ng.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service managing asynchroneous tasks as dataset import.
 * Manages a Task cache
 * @author JCome
 *
 */
@Service
public class AsyncTaskService {

	/** The task cache. */
	private final Map<Long, List<AsyncTask>> taskCache = new ConcurrentHashMap<>();

	/**
	 * Gets a list of tasks associated to a user
	 * @param userId the user to get the tasks from
	 * @return An empty array list if no tasks found, the list of tasks otherwise;
	 */
	public List<AsyncTask> getTasks(Long userId) {
		return taskCache.get(userId) != null ? taskCache.get(userId) : Collections.emptyList();
	}

	/**
	 * Adds a task to a user's list of tasks
	 * @param userId the user to add a task to
	 * @param task the task to add
	 */
	public void addTask(AsyncTask task) {
		Long userId = task.getUser();
		taskCache.computeIfAbsent(userId, smthing -> new ArrayList<>());
		boolean found = false;
		for (int index = 0; index < taskCache.get(userId).size(); index++) {
		    if(taskCache.get(userId).get(index).getId().equals(task.getId())){
		    	taskCache.get(userId).set(index, task);
		    	found = true;
		        break;
		    }
		}
		if (!found) {
			taskCache.get(userId).add(task);
		}
	}

	/**
	 * Delete all old tasks every 6 hours
	 */
	@Scheduled(fixedDelay = 248400000)
	public void clearTasks() {
		for(Entry<Long, List<AsyncTask>> entry : taskCache.entrySet()) {
			Iterator<AsyncTask> taskIterator = entry.getValue().iterator();
			while (taskIterator.hasNext()) {
				AsyncTask task = taskIterator.next();
				if (task.isDeletable()) {
					taskIterator.remove();
				}
			}
		}
	}
}
