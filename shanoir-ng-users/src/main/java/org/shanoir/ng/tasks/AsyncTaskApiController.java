package org.shanoir.ng.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    private ShanoirEventsService taskService;

    public static final List<UserSseEmitter> EMITTERS = Collections.synchronizedList(new ArrayList<>());

    @Override
    public ResponseEntity<List<ShanoirEventLight>> findTasks() {
        Long userId = KeycloakUtil.getTokenUserId();
        List<ShanoirEventLight> taskList = taskService.getEventsByUserAndType(
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
        Comparator<ShanoirEventLight> comparator = new Comparator<>() {
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
        EMITTERS.add(emitter);
        emitter.onCompletion(() -> EMITTERS.remove(emitter));
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }
}
