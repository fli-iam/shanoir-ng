/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
 * @author fli
 *
 */
@Service
public class ShanoirEventsService {

    @Autowired
    private ShanoirEventRepository repository;

    @Autowired
    private ShanoirEventRepositoryCustom repositoryCustom;

    @Autowired
    private UserRepository userRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ShanoirEventsService.class);

    public static final long INACTIVE_TIMEOUT = 5 * DateUtils.MILLIS_PER_MINUTE;

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
                || ShanoirEventType.DELETE_DATASET_EVENT.equals(event.getEventType())
                || ShanoirEventType.DELETE_EXAMINATION_EVENT.equals(event.getEventType())
                || ShanoirEventType.DELETE_NIFTI_EVENT.equals(event.getEventType())) {
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
        List<ShanoirEvent> dbEvents = Utils.toList(repository.findByUserIdAndEventTypeInAndLastUpdateYoungerThan7Days(userId, list));
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
            event.setMessage("Inactivity timeout, task was set to error status because inactive for more than 5 minutes.");
            return event;
        }).collect(Collectors.toList());
        if (!updatedEvents.isEmpty()) repository.saveAll(updatedEvents);
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
     * Sends an event using an emiter
     * @param notification the event to send
     */
    public void sendSseEventsToUI(ShanoirEvent notification) {
        List<UserSseEmitter> sseEmitterListToRemove = new ArrayList<>();
        AsyncTaskApiController.EMITTERS.forEach((UserSseEmitter emitter) -> {
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
        AsyncTaskApiController.EMITTERS.removeAll(sseEmitterListToRemove);
    }

    @Scheduled(fixedDelay = 30000)
    private void keepConnectionAlive() {
        List<SseEmitter> sseEmitterListToRemove = new ArrayList<>();
        AsyncTaskApiController.EMITTERS.forEach((SseEmitter emitter) -> {
            try {
                emitter.send("{}", MediaType.APPLICATION_JSON);
            } catch (Exception e) {
                // This happens when the user's connection reset, do not log anything.
                emitter.complete();
                sseEmitterListToRemove.add(emitter);
            }
        });
        AsyncTaskApiController.EMITTERS.removeAll(sseEmitterListToRemove);
    }

    public ShanoirEvent findById(Long taskId) {
        Long userId = KeycloakUtil.getTokenUserId();
        return repository.findByIdAndUserId(taskId, userId);
    }

    public Page<ShanoirEvent> findByStudyId(final Pageable pageable, Long studyId, String searchStr, String searchField) {
        Page<ShanoirEvent> events = repositoryCustom.findByStudyIdOrderByCreationDateDescAndSearch(pageable, studyId, searchStr, searchField);
        return events;
    }

    /**
	   * Count number of events happened in the last x days
	   * to display statistic on welcome page
	   *
	   * @param days number of days
	   * @return number of events
	   */
	  public Long countPassedEvents(Integer days) {
		    return repository.countByLastUpdateAfter(new Date(System.currentTimeMillis() - days * DateUtils.MILLIS_PER_DAY));
	  }
}
