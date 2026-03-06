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

package org.shanoir.ng.shared.email;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public abstract class EmailBase {

    private Long userId;

    private List<Long> recipients;

    private Long studyId;

    private String studyCardId;

    private String studyName;

    @JsonProperty("userId")
    public Long getUserId() {
        return userId;
    }

    @JsonSetter("userId")
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @JsonProperty("recipients")
    public List<Long> getRecipients() {
        return recipients;
    }

    @JsonSetter("recipients")
    public void setRecipients(List<Long> recipients) {
        this.recipients = recipients;
    }

    @JsonProperty("studyId")
    public Long getStudyId() {
        return studyId;
    }

    @JsonSetter("studyId")
    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    @JsonProperty("studyCardId")
    public String getStudyCardId() {
        return studyCardId;
    }

    @JsonSetter("studyCardId")
    public void setStudyCardId(String studyCardId) {
        this.studyCardId = studyCardId;
    }

    @JsonProperty("studyName")
    public String getStudyName() {
        return studyName;
    }

    @JsonSetter("studyName")
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

}
