package org.shanoir.ng.tasks;

import java.util.UUID;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.LocalDateTimeSerializer;

/**
 * Asynchroneous tasks for data loading.
 * @author JCome
 *
 */
public class AsyncTask {

	private String id;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using=LocalDateTimeDeserializer.class)
	private LocalDateTime startDate;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using=LocalDateTimeDeserializer.class)
	private LocalDateTime endDate;

	private float progress;

	private String label;

	private String message;

	private Long user;
	
	public AsyncTask() {
		
	}

	public AsyncTask(String label, Long user) {
		super();
		this.label = label;
		this.user = user;
		this.progress = 0;
		this.startDate = LocalDateTime.now();
		this.endDate = null;
		this.message = "Just created";
		this.id = UUID.randomUUID().toString();
	}
	
	public AsyncTask(String id, LocalDateTime startDate, LocalDateTime endDate, float progress, String label, String message, Long user) {
		super();
		this.id = id;
		this.startDate = startDate;
		this.endDate = endDate;
		this.progress = progress;
		this.label = label;
		this.message = message;
		this.user = user;
	}

	/***
	 * Updates a task with the given message and progress status
	 * @param progress the progress to set
	 * @param message the message to set
	 */
	public void updateTask(float progress, String message) {
		this.progress = progress;
		this.message = message;
	}

	/**
	 * Compute whether a task is deletable or not:
	 * If in error => keep it 7 days
	 * If finished => keep it 7 days
	 * If in progress for more than 7 days => put it in error and don't delete it for the moment
	 * Otherwise, don't delete it
	 * @return
	 */
	public boolean isDeletable() {
		if (isInProgress() && this.startDate.plusDays(7).compareTo(LocalDateTime.now()) < 0 ) {
			this.setProgress(-1);
			this.setEndDate(LocalDateTime.now());
			this.message = "This task was set in error due to a too long treatment: more than 7 days.";
		}
		return isFinished() && this.endDate.plusDays(7).compareTo(LocalDateTime.now()) < 0
			|| isInError() && this.startDate.plusDays(7).compareTo(LocalDateTime.now()) < 0;
	}

	public void endTask() {
		this.endDate = LocalDateTime.now();
		this.progress = 1;
		this.message = "Succes";
	}

	public void setInError(String message) {
		this.progress  = -1;
		this.message = message;
		this.endDate = LocalDateTime.now();
	}

	/**
	 * @return the startDate
	 */
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the progress
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(float progress) {
		this.progress = progress;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
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
	 * @return the user
	 */
	public Long getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(Long user) {
		this.user = user;
	}

	public boolean isInProgress() {
		return progress > 0 && progress < 1;
	}

	public boolean isInError() {
		return progress == -1;
	}

	public boolean isFinished() {
		return progress == 1;
	}

	public String getId( ) {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
