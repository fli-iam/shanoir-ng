package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
		Long userId = KeycloakUtil.getTokenUserId();
		List<ShanoirEvent> taskList = taskService.getEventsByUserAndType(userId, ShanoirEventType.IMPORT_DATASET_EVENT);
		
		// Get only event with last updates < 7 days
		Date now = new Date();
		Long nowMinusSevenDays = now.getTime() - 7 * DateUtils.MILLIS_PER_DAY;
 		taskList = taskList.stream().filter(event -> event.getLastUpdate().getTime() > nowMinusSevenDays).collect(Collectors.toList());

 		// Order by last update date
		Comparator<ShanoirEvent> comparator = new Comparator<ShanoirEvent>() {
			@Override
			public int compare(ShanoirEvent event1, ShanoirEvent event2) {
				return event1.getLastUpdate().before(event2.getLastUpdate()) ? 1 : -1;
			}
		};
		taskList.sort(comparator);

		return new ResponseEntity<>(taskList, HttpStatus.OK);
	}

	@Override
    public ResponseEntity<SseEmitter> updateTasks() throws IOException {
        SseEmitter emitter = new SseEmitter(-1L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        return new ResponseEntity<>(emitter,HttpStatus.OK);
    }
}
