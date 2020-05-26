package org.shanoir.ng.events;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShanoirEventRepository extends CrudRepository<ShanoirEvent, Long> {

	/**
	 * Find event by userId and eventType
	 *
	 * @param userId
	 * @param eventType
	 * @return a list of ShanoirEvents with given userID and event type
	 */
	List<ShanoirEvent> findByUserIdAndEventType(Long userId, String eventType);

	/**
	 * Deletes all events older than a date.
	 * @param expiryDate the expiration date.
	 */
    public void deleteByLastUpdateBefore(Date expiryDate);
}
