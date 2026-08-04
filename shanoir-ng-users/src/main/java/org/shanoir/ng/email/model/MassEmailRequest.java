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

package org.shanoir.ng.email.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to email a group of users: a platform-wide group for an administrator,
 * or the members of a single study for its administrators.
 *
 * @author afragkiadakis
 */
public class MassEmailRequest {

    @NotNull
    private RecipientGroup recipientGroup;

    @NotBlank
    private String subject;

    @NotBlank
    private String content;

    private Long studyId;

    /**
     * @return the recipientGroup
     */
    public RecipientGroup getRecipientGroup() {
        return recipientGroup;
    }

    /**
     * @param recipientGroup
     *            the recipientGroup to set
     */
    public void setRecipientGroup(RecipientGroup recipientGroup) {
        this.recipientGroup = recipientGroup;
    }

    /**
     * @return the studyId
     */
    public Long getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            the studyId to set
     */
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    /**
     * studyId must be provided when targeting the STUDY group, and must be
     * absent for every other group. The recipients themselves are always
     * resolved server side, never sent by the caller.
     *
     * @return whether studyId is consistent with recipientGroup
     */
    @AssertTrue(message = "studyId is required when recipientGroup is STUDY, and must not be set otherwise")
    public boolean isStudyIdConsistent() {
        return (RecipientGroup.STUDY == recipientGroup) == (studyId != null);
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

}
