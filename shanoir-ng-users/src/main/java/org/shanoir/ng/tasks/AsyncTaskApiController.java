package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventLight;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * API to manage asynchronous tasks:
 * - Retrieve a list of tasks for a user
 * 
 * @author fli
 *
 */
@Controller
public class AsyncTaskApiController implements AsyncTaskApi {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncTaskApiController.class);

	public static final List<UserSseEmitter> emitters = new CopyOnWriteArrayList<UserSseEmitter>();

	@Autowired
	ShanoirEventsService eventsService;

	@Override
	public ResponseEntity<List<ShanoirEventLight>> findTasks() {
		Long userId = KeycloakUtil.getTokenUserId();
		List<ShanoirEventLight> taskList = eventsService.getEventsByUserAndType(
				userId,
				ShanoirEventType.IMPORT_DATASET_EVENT,
				ShanoirEventType.COPY_DATASET_EVENT,
				ShanoirEventType.EXECUTION_MONITORING_EVENT,
				ShanoirEventType.CHECK_QUALITY_EVENT,
				ShanoirEventType.SOLR_INDEX_ALL_EVENT,
				ShanoirEventType.DOWNLOAD_STATISTICS_EVENT,
				ShanoirEventType.DELETE_EXAMINATION_EVENT,
				ShanoirEventType.DELETE_DATASET_EVENT);
		// Order by last update date
		Comparator<ShanoirEventLight> comparator = new Comparator<ShanoirEventLight>() {
			@Override
			public int compare(ShanoirEventLight event1, ShanoirEventLight event2) {
				return event1.getLastUpdate().before(event2.getLastUpdate()) ? 1 : -1;
			}
		};
		taskList.sort(comparator);
		return new ResponseEntity<>(taskList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ShanoirEvent> getTaskDetails(
			@Parameter(name = "id of the task", required = true) @PathVariable("taskId") Long taskId) {
		ShanoirEvent event = eventsService.findById(taskId);
		if (event == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(event, HttpStatus.OK);
		}
	}

	/**
	 * As an user in his browser can open multiple tabs, it is wanted
	 * that multiple emitters exist with the same userId.
	 */
	@Override
	public ResponseEntity<SseEmitter> updateTasks() throws IOException {
		UserSseEmitter emitter = new UserSseEmitter(KeycloakUtil.getTokenUserId());
		emitters.add(emitter);
		LOG.info("UserSseEmitter added for user " + KeycloakUtil.getTokenUserName());
		return new ResponseEntity<>(emitter, HttpStatus.OK);
	}

}
