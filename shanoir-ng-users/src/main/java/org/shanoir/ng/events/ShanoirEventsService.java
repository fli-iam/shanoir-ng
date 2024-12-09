package org.shanoir.ng.events;

import org.apache.commons.lang3.time.DateUtils;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.tasks.AsyncTaskApiController;
import org.shanoir.ng.tasks.UserSseEmitter;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service managing ShanoirEvents
 * @author fli
 *
 */
@Service
public class ShanoirEventsService {

	@Autowired
	ShanoirEventRepository repository;
	@Autowired
	ShanoirEventRepositoryCustom repositoryCustom;

	@Autowired
	UserRepository userRepository;

	private static final Logger LOG = LoggerFactory.getLogger(ShanoirEventsService.class);

	public void addEvent(ShanoirEvent event) {
		// Call repository
		repository.save(event);
		// This is sad but with the @CreationTimestamp the date is not returned by the save method 
		ShanoirEvent saved = repository.findById(event.getId()).orElse(null);
		// Push notification to UI
		if (ShanoirEventType.IMPORT_DATASET_EVENT.equals(event.getEventType())
			  || ShanoirEventType.EXECUTION_MONITORING_EVENT.equals(event.getEventType())
				|| ShanoirEventType.SOLR_INDEX_ALL_EVENT.equals(event.getEventType())
				|| ShanoirEventType.COPY_DATASET_EVENT.equals(event.getEventType())
				|| ShanoirEventType.CHECK_QUALITY_EVENT.equals(event.getEventType())
				|| ShanoirEventType.DOWNLOAD_STATISTICS_EVENT.equals(event.getEventType())
				|| ShanoirEventType.DELETE_EXAMINATION_EVENT.equals(event.getEventType())
				|| ShanoirEventType.DELETE_NIFTI_EVENT.equals(event.getEventType())) {
			sendSseEventsToUI(saved);
		}
	}

	public List<ShanoirEvent> getEventsByUserIdAndTypeIn(Long userId, List<String> eventType) {
		return Utils.toList(repository.findByUserIdAndEventTypeIn(userId, eventType));
	}

	public List<ShanoirEvent> getEventsByObjectIdAndTypeIn(String objectId, String eventType) {
		return Utils.toList(repository.findByObjectIdAndEventType(objectId, eventType));
  }
    
	public List<ShanoirEventLight> getEventsByUserAndType(Long userId, String... eventType) {
		List<String> list = new ArrayList<String>();
		for (String type : eventType) {
			list.add(type);
		}
		List<ShanoirEventLight> events = new ArrayList<>();
		for (ShanoirEvent event : Utils.toList(repository.findByUserIdAndEventTypeIn(userId, list))) {
			events.add(event.toLightEvent());
		}
		return events;
	}

	/**
	 * Deletes everyday events older than 1 year.
	 */
	@Scheduled(fixedDelay = DateUtils.MILLIS_PER_DAY)
	private void deletePeriodically( ) {
		Date now = new Date();
		Long nowMinusOneYear = now.getTime() - DateUtils.MILLIS_PER_DAY * 361;
		repository.deleteByLastUpdateBefore(new Date(nowMinusOneYear));
	}

	/**
	 * Sends an event using an emiter
	 * @param notification the event to send
	 */
	public void sendSseEventsToUI(ShanoirEvent notification) {
        List<UserSseEmitter> sseEmitterListToRemove = new ArrayList<>();
        AsyncTaskApiController.emitters.forEach((UserSseEmitter emitter) -> {
			// ! IMPORTANT filter on user id
			if (notification.getUserId() != null && notification.getUserId().equals(emitter.getUserId())) {
				if (notification.getLastUpdate() == null) {
					notification.setLastUpdate(new Date());
				}
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
            	// This happens when the user's connection reset, do not log anything.
            	emitter.complete();
                sseEmitterListToRemove.add(emitter);
            }
        });
        AsyncTaskApiController.emitters.removeAll(sseEmitterListToRemove);
	}

	public ShanoirEvent findById(Long taskId) {
		Long userId = KeycloakUtil.getTokenUserId();
		return repository.findByIdAndUserId(taskId, userId);
	}

	public Page<ShanoirEvent> findByStudyId(final Pageable pageable,Long studyId, String searchStr, String searchField) {
		Page<ShanoirEvent> events = repositoryCustom.findByStudyIdOrderByCreationDateDescAndSearch(pageable, studyId, searchStr, searchField);
		return events;
	}
}
