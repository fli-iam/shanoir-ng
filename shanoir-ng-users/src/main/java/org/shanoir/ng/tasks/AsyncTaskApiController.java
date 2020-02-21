package org.shanoir.ng.tasks;

import java.util.List;

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
	AsyncTaskService taskService;

	@Override
	public ResponseEntity<List<AsyncTask>> findTasks() {
		Long userId = KeycloakUtil.getTokenUserId();
		return new ResponseEntity<>(taskService.getTasks(userId), HttpStatus.OK);
	}
}
