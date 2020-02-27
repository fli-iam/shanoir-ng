package org.shanoir.ng.events;

import java.util.List;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service managing ShanoirEvents
 * @author fli
 *
 */
@Service
public class ShanoirEventsService {

	@Autowired
	ShanoirEventRepository repository;

	public void addEvent(ShanoirEvent event) {
		// Call repository
		repository.save(event);
	}

	public List<ShanoirEvent> getEventsByUserAndType(Long userId, String eventType) {
		return Utils.toList(repository.findByUserIdAndEventType(userId, eventType));
	}
}
