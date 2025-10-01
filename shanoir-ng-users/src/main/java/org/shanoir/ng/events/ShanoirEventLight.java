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

import java.sql.Types;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * A shanoir event is an event allowing to keep some history during CRUD events of shanoir elements
 * @author JComeD
 *
 */
@MappedSuperclass
public class ShanoirEventLight {

    /** ID of the event, normally generated BEFORE arriving here **/
    @Id
    protected Long id;

    /** See EventType **/
    protected String eventType;

    /** ID of the concerned object **/
    protected String objectId;

    /** user ID creating the event **/
    protected Long userId;

    /** Message of the event, can be informative, or display an error **/
    @JdbcTypeCode(Types.LONGVARCHAR)
    protected String message;

    /** Creation date, automatically generated **/
    @CreationTimestamp
    @Column(updatable = false)
    protected Date creationDate;

    /** Last update date, automatically generated **/
    @UpdateTimestamp
    protected Date lastUpdate;

    /** Status, can be either 0 (created), 1 (success) or -1 (in error) **/
    protected int status;

    /** The progress of the event. */
    protected Float progress;

    /** The study ID of the event */
    protected Long studyId;

    @Transient
    private Boolean hasReport;

    public ShanoirEventLight() {
        // Default empty constructor for json deserializer.
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
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the lastUpdate
     */
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
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

    public Boolean getHasReport() {
        return hasReport;
    }

    public void setHasReport(Boolean hasReport) {
        this.hasReport = hasReport;
    }

    public String getIdAsString() {
        return id == null ? null : id.toString();
    }

}
