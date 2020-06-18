package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.time.DateUtils;
import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.annotations.ApiParam;

/**
 * API to manage asynchronous tasks:
 * - Retrieve a list of tasks for a user
 * @author fli
 *
 */
@Controller
public class AsyncTaskApiController implements AsyncTaskApi {

	@Autowired
	ShanoirEventsService taskService;

	public static final List<SseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());

	@Override
	public ResponseEntity<List<ShanoirEvent>> findTasks() {
		String[] eventTypes = {ShanoirEventType.IMPORT_DATASET_EVENT};
		return findTasksByType(eventTypes);
	}

	@Override
	public ResponseEntity<List<ShanoirEvent>> findTasksByType (
			@ApiParam(value = "types of events to retrieve", required=true) @Valid
			@RequestParam(value = "types", required = true) String[] types) {
		Long userId = KeycloakUtil.getTokenUserId();

		List<ShanoirEvent> taskList = taskService.getEventsByUserAndType(userId, types);

		taskList = filterByDateAndOrder(taskList, 7);

		if (taskList.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		return new ResponseEntity<>(taskList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SseEmitter> updateTasks() throws IOException {
		SseEmitter emitter = new SseEmitter(-1L);
		emitters.add(emitter);
		emitter.onCompletion(() -> emitters.remove(emitter));
		return new ResponseEntity<>(emitter,HttpStatus.OK);
	}

	/**
	 * This method filter a list of event by dates (number of days before today) and order them
	 * @param events the liost of events to filter / order
	 * @param days the number of days before today we want the event
	 * @return a filtered and ordered lsiot of events
	 */
	private List<ShanoirEvent> filterByDateAndOrder(List<ShanoirEvent> events, int days) {
		if (events == null || events.isEmpty()) {
			return Collections.emptyList();
		}
		// Get only event with last updates < 7 days
		Date now = new Date();
		Long nowMinusSevenDays = now.getTime() - days * DateUtils.MILLIS_PER_DAY;
		events = events.stream().filter(event -> event.getLastUpdate().getTime() > nowMinusSevenDays).collect(Collectors.toList());

		// Order by last update date
		Comparator<ShanoirEvent> comparator = new Comparator<ShanoirEvent>() {
			@Override
			public int compare(ShanoirEvent event1, ShanoirEvent event2) {
				if (event1.getLastUpdate().equals(event2.getLastUpdate())) {
					return 0;
				}
				return event1.getLastUpdate().before(event2.getLastUpdate()) ? 1 : -1;
			}
		};
		events.sort(comparator);

		return events;
	}
}
