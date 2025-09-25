package org.shanoir.ng.shared.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Transient;

import java.util.Map;
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

	protected String report;

	protected int status;

	protected Float progress;

	protected Long studyId;
	
	private Long timestamp;

	@Transient
	@JsonProperty("eventProperties")
	private Map<String, String> eventProperties;

   
	public ShanoirEvent() {
	}

	public ShanoirEvent(String eventType, String objectId, Long userId, String message,	int status) {
		this.eventType = eventType;
		setObjectId(objectId);
		this.userId = userId;
		this.message = message;
		this.status = status;
		// Generate an ID
		this.id = generateId();
	}

	public ShanoirEvent(String eventType, String objectId, Long userId, String message,	int status, Long studyId) {
		this(generateId(), eventType, objectId, userId, message, status, studyId);
	}

	public ShanoirEvent(Long id, String eventType, String objectId, Long userId, String message, int status, Long studyId) {
		this.eventType = eventType;
		setObjectId(objectId);
		this.userId = userId;
		this.message = message;
		this.status = status;
		this.studyId = studyId;
		this.id = id;
	}

	public ShanoirEvent(String eventType, String objectId, Long userId, String message,	int status, float progress) {
		this(eventType, objectId, userId, message, status);
		this.progress = Float.valueOf(progress);
	}

	public ShanoirEvent(String eventType, String objectId, Long userId, String message,	int status, float progress, Long studyId) {
		this(eventType, objectId, userId, message, status, studyId);
		this.progress = Float.valueOf(progress);
	}

	public static Long generateId() {
		return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
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
		if (objectId != null && objectId.length() > 255) {
			this.objectId = objectId.substring(0, 250) + "...";
		} else {
			this.objectId = objectId;
		}
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
		this.message = message == null ? null : message.replaceAll("\uFFFD", "?");
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		//.replaceAll("[^a-zA-Z0-9]+", "");
		this.report = report == null ? null : report.replaceAll("\uFFFD", "?");
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
    
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

	public Map<String, String> getEventProperties() {
		return eventProperties;
	}

	public void setEventProperties(Map<String, String> eventProperties) {
		this.eventProperties = eventProperties;
	}
}
