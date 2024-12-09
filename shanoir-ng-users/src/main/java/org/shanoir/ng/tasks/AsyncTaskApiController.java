package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventLight;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
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
 * @author fli
 *
 */
@Controller
public class AsyncTaskApiController implements AsyncTaskApi {

	@Autowired
	ShanoirEventsService taskService;

    public static final List<UserSseEmitter> emitters = Collections.synchronizedList(new ArrayList<>());

	@Override
	public ResponseEntity<List<ShanoirEventLight>> findTasks() {
		Long userId = KeycloakUtil.getTokenUserId();

		List<ShanoirEventLight> taskList = taskService.getEventsByUserAndType(userId, ShanoirEventType.IMPORT_DATASET_EVENT, ShanoirEventType.COPY_DATASET_EVENT, ShanoirEventType.EXECUTION_MONITORING_EVENT, ShanoirEventType.CHECK_QUALITY_EVENT, ShanoirEventType.SOLR_INDEX_ALL_EVENT, ShanoirEventType.DOWNLOAD_STATISTICS_EVENT, ShanoirEventType.DELETE_EXAMINATION_EVENT, ShanoirEventType.DELETE_NIFTI_EVENT);
    
		// Get only event with last updates < 7 days
		Date now = new Date();
		Long nowMinusSevenDays = now.getTime() - 7 * DateUtils.MILLIS_PER_DAY;
 		taskList = taskList.stream().filter(event -> event.getLastUpdate().getTime() > nowMinusSevenDays).collect(Collectors.toList());

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
		ShanoirEvent event = taskService.findById(taskId);
		if (event == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(event, HttpStatus.OK);
		}
	}

	@Override
    public ResponseEntity<SseEmitter> updateTasks() throws IOException {
	long userId = KeycloakUtil.getTokenUserId();
        UserSseEmitter emitter = new UserSseEmitter(userId);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        return new ResponseEntity<>(emitter,HttpStatus.OK);
    }
}
