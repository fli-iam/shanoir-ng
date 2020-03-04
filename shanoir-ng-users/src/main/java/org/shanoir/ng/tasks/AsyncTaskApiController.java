package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		return new ResponseEntity<>(taskService.getEventsByUserAndType(userId, ShanoirEventType.IMPORT_DATASET_EVENT), HttpStatus.OK);
	}

	@Override
    public ResponseEntity<SseEmitter> updateTasks() throws IOException {
        SseEmitter emitter = new SseEmitter(-1L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        return new ResponseEntity<>(emitter,HttpStatus.OK);
    }
}
