package org.shanoir.ng.events;

public class UserDeleteEvent {

	private Long userId;

	public UserDeleteEvent(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}
	
}
