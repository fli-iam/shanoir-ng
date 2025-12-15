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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShanoirEventRepository extends CrudRepository<ShanoirEvent, Long> {

    public static final int TIMEOUT_DAYS = 7;

    /**
     * Find event by userId and eventType
     *
     * @param userId
     * @param eventType
     * @return a list of ShanoirEvents with given userID and event type
     */
    List<ShanoirEvent> findByUserIdAndEventTypeInAndLastUpdateGreaterThan(Long userId, List<String> eventType, Date earlyDateThreshold);

    default List<ShanoirEvent> findByUserIdAndEventTypeInAndLastUpdateYoungerThan7Days(Long userId, List<String> eventType) {
        return findByUserIdAndEventTypeInAndLastUpdateGreaterThan(userId, eventType, DateUtils.addDays(new Date(), -1 * TIMEOUT_DAYS));
    }

    /**
     * Deletes all events older than a date.
     *
     * @param expiryDate the expiration date.
     */
    public void deleteByLastUpdateBefore(Date expiryDate);

    List<ShanoirEvent> findByObjectIdAndEventType(String objectId, String eventType);

    ShanoirEvent findByIdAndUserId(Long taskId, long userId);


}
