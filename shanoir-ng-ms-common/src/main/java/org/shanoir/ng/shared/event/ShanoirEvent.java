package org.shanoir.ng.shared.event;

import java.util.UUID;

public class ShanoirEvent {

	public static final int ERROR = -1;
	public static final int SUCCESS = 1;
	public static final int IN_PROGRESS = 2;

	protected Long id;
	
	protected String eventType;
	
	protected String objectId;

	protected Long userId;

	protected String message;

	protected int status;

	protected Float progress;

	protected Long studyId;

	public ShanoirEvent() {
	}

	public ShanoirEvent(String eventType, String objectId, Long userId, String message,	int status) {
		this.eventType = eventType;
		this.objectId = objectId;
		this.userId = userId;
		this.message = message;
		this.status = status;
		// Generate an ID
		this.id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
	}

	public ShanoirEvent(String eventType, String objectId, Long userId, String message,	int status, Long studyId) {
		this.eventType = eventType;
		this.objectId = objectId;
		this.userId = userId;
		this.message = message;
		this.status = status;
		this.studyId = studyId;
		// Generate an ID
		this.id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
	}

	public ShanoirEvent(String eventType, String objectId, Long userId, String message,	int status, float progress) {
		this(eventType, objectId, userId, message, status);
		this.progress = Float.valueOf(progress);
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the objectId
	 */
	public String getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the progress
	 */
	public Float getProgress() {
		return progress;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(Float progress) {
		this.progress = progress;
	}

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

}
