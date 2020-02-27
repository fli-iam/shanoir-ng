package org.shanoir.ng.tasks;

import java.util.List;

import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

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

	@Override
	public ResponseEntity<List<ShanoirEvent>> findTasks() {
		Long userId = KeycloakUtil.getTokenUserId();
		return new ResponseEntity<>(taskService.getEventsByUserAndType(userId, ShanoirEventType.IMPORT_DATASET_EVENT), HttpStatus.OK);
	}
}
