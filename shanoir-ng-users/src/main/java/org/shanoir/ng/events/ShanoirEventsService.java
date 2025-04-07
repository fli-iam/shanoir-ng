package org.shanoir.ng.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

/**
 * Service managing ShanoirEvents
 * 
 * @author fli
 *
 */
@Service
public class ShanoirEventsService {

	private static final Logger LOG = LoggerFactory.getLogger(ShanoirEventsService.class);

	public static final long INACTIVE_TIMEOUT = 5 * DateUtils.MILLIS_PER_MINUTE;

	@Autowired
	ShanoirEventRepository repository;

	@Autowired
	ShanoirEventRepositoryCustom repositoryCustom;

	@Autowired
	UserRepository userRepository;

	public void addEvent(ShanoirEvent event) {
		// Call repository
		repository.save(event);
		// This is sad but with the @CreationTimestamp the date is not returned by the
		// save method
		ShanoirEvent saved = repository.findById(event.getId()).orElse(null);
		// Push notification to UI
		if (ShanoirEventType.IMPORT_DATASET_EVENT.equals(event.getEventType())
				|| ShanoirEventType.EXECUTION_MONITORING_EVENT.equals(event.getEventType())
				|| ShanoirEventType.SOLR_INDEX_ALL_EVENT.equals(event.getEventType())
				|| ShanoirEventType.COPY_DATASET_EVENT.equals(event.getEventType())
				|| ShanoirEventType.CHECK_QUALITY_EVENT.equals(event.getEventType())
				|| ShanoirEventType.DOWNLOAD_STATISTICS_EVENT.equals(event.getEventType())
				|| ShanoirEventType.DELETE_EXAMINATION_EVENT.equals(event.getEventType())
				|| ShanoirEventType.DELETE_DATASET_EVENT.equals(event.getEventType())) {
			sendSseEventsToUI(saved);
		}
	}

	public List<ShanoirEvent> getEventsByObjectIdAndTypeIn(String objectId, String eventType) {
		return Utils.toList(repository.findByObjectIdAndEventType(objectId, eventType));
	}

	/**
	 * Get events younger than 7 days
	 */
	public List<ShanoirEventLight> getEventsByUserAndType(Long userId, String... eventType) {
		List<String> list = new ArrayList<String>();
		for (String type : eventType) {
			list.add(type);
		}
		List<ShanoirEvent> dbEvents = Utils
				.toList(repository.findByUserIdAndEventTypeInAndLastUpdateYoungerThan7Days(userId, list));
		List<ShanoirEventLight> events = new ArrayList<>();
		cleanEvents(dbEvents);
		for (ShanoirEvent event : dbEvents) {
			events.add(event.toLightEvent());
		}
		return events;
	}

	/**
	 * Set inactive event that are still in a running status to error status
	 */
	private void cleanEvents(List<ShanoirEvent> events) {
		Long now = new Date().getTime();
		// set inactive tasks since > 5 min with a running status
		List<ShanoirEvent> updatedEvents = events.stream().filter(event -> {
			return (event.getStatus() == 2 || event.getStatus() == 5)
					&& now - event.getLastUpdate().getTime() > INACTIVE_TIMEOUT;
		}).map(event -> {
			event.setStatus(-1);
			event.setMessage("inactivity timeout, there must has been");
			return event;
		}).collect(Collectors.toList());
		if (!updatedEvents.isEmpty())
			repository.saveAll(updatedEvents);
	}

	/**
	 * Deletes everyday events older than 1 year.
	 */
	@Scheduled(fixedDelay = DateUtils.MILLIS_PER_DAY)
	private void deletePeriodically() {
		Date now = new Date();
		Long nowMinusOneYear = now.getTime() - DateUtils.MILLIS_PER_DAY * 361;
		repository.deleteByLastUpdateBefore(new Date(nowMinusOneYear));
	}

	/**
	 * Sends an event using an emitter.
	 * 
	 * @param notification the event to send
	 */
	public void sendSseEventsToUI(ShanoirEvent notification) {
		List<UserSseEmitter> emitters = AsyncTaskApiController.emitters;
		for (UserSseEmitter userSseEmitter : emitters) {
			// ! IMPORTANT filter on user id
			if (notification.getUserId() != null && notification.getUserId().equals(userSseEmitter.getUserId())) {
				if (notification.getLastUpdate() == null) {
					notification.setLastUpdate(new Date());
				}
				try {
					userSseEmitter.send(notification, MediaType.APPLICATION_JSON);
				} catch (Exception e) {
					LOG.error("sendSseEventsToUI: error while sending data for user {}", userSseEmitter.getUserId());
					emitters.remove(userSseEmitter);
				}
			}
		}
	}

	/**
	 * Attention: while the keep alive loop is running all 30 seconds and
	 * sends an empty message to each browser-tab, a new user can log in and
	 * manipulate the emitters list, that is why we need to be careful with
	 * the thread-safety. CopyOnWriteArrayList allows to safely iterate, while
	 * the below ArrayList is manipulated.
	 */
	@Scheduled(fixedDelay = 30000)
	private void keepConnectionAlive() {
		List<UserSseEmitter> emitters = AsyncTaskApiController.emitters;
		for (UserSseEmitter userSseEmitter : emitters) {
			try {
				userSseEmitter.send("{}", MediaType.APPLICATION_JSON);
			} catch (Exception e) {
				LOG.error("Keep-Alive-SSE: error while sending data for user {}", userSseEmitter.getUserId());
				emitters.remove(userSseEmitter);
			}
		}
		LOG.info("Keep-Alive-SSE: {} userIds: {}", emitters.size(), emitters.toString());
	}

	public ShanoirEvent findById(Long taskId) {
		Long userId = KeycloakUtil.getTokenUserId();
		return repository.findByIdAndUserId(taskId, userId);
	}

	public Page<ShanoirEvent> findByStudyId(final Pageable pageable, Long studyId, String searchStr,
			String searchField) {
		Page<ShanoirEvent> events = repositoryCustom.findByStudyIdOrderByCreationDateDescAndSearch(pageable, studyId,
				searchStr, searchField);
		return events;
	}

}
