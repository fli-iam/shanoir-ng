package org.shanoir.ng.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.tasks.AsyncTaskApiController;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service managing ShanoirEvents
 * @author fli
 *
 */
@Service
public class ShanoirEventsService {

	@Autowired
	ShanoirEventRepository repository;

	private static final Logger LOG = LoggerFactory.getLogger(ShanoirEventsService.class);


	public void addEvent(ShanoirEvent event) {
		// Call repository
		repository.save(event);

		// Push notification to UI
		if (ShanoirEventType.IMPORT_DATASET_EVENT.equals(event.getEventType())) {
			sendSseEventsToUI(event);
		}
	}

	public List<ShanoirEvent> getEventsByUserAndType(Long userId, String eventType) {
		return Utils.toList(repository.findByUserIdAndEventType(userId, eventType));
	}

	/**
	 * Deletes everyday events older than 1 year.
	 */
	@Scheduled(fixedDelay = DateUtils.MILLIS_PER_DAY)
	private void deletePeriodically( ) {
		Date now = new Date();
		Long nowMinusSevenDays = now.getTime() - DateUtils.MILLIS_PER_DAY * 180;
		repository.deleteByLastUpdateBefore(new Date(nowMinusSevenDays));
	}

	/**
	 * Sends an event using an emiter
	 * @param notification the event to send
	 */
	public void sendSseEventsToUI(ShanoirEvent notification) {
        List<SseEmitter> sseEmitterListToRemove = new ArrayList<>();
        AsyncTaskApiController.emitters.forEach((SseEmitter emitter) -> {
            try {
                emitter.send(notification, MediaType.APPLICATION_JSON);
            } catch (IOException e2) {
            	emitter.complete();
                sseEmitterListToRemove.add(emitter);
                LOG.error("Error while send task to UI ", e2);
            } catch (Exception e) {
            	emitter.complete();
                sseEmitterListToRemove.add(emitter);
                LOG.error("Error while send task to UI ", e);
                throw e;
            }
        });
        AsyncTaskApiController.emitters.removeAll(sseEmitterListToRemove);
    }

	@Scheduled(fixedDelay = 30000)
	private void keepConnectionAlive( ) {
        List<SseEmitter> sseEmitterListToRemove = new ArrayList<>();
        AsyncTaskApiController.emitters.forEach((SseEmitter emitter) -> {
            try {
                emitter.send("{}", MediaType.APPLICATION_JSON);
            } catch (Exception e) {
            	emitter.complete();
                sseEmitterListToRemove.add(emitter);
                LOG.error("Error while keeping connection alive. ", e);
            }
        });
        AsyncTaskApiController.emitters.removeAll(sseEmitterListToRemove);
	}
}
