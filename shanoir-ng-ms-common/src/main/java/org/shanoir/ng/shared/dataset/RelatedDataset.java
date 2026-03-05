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

package org.shanoir.ng.shared.dataset;

import java.util.List;
import java.util.Map;

import org.shanoir.ng.utils.KeycloakUtil.UserRole;

public class RelatedDataset {

    private Long studyId;

    private Map<Long, Long> subjectMapping;

    private List<Long> datasetIds;

    private Long userId;

    private UserRole userRole;

    private Long eventId;

    public RelatedDataset() {
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public Map<Long, Long> getSubjectMapping() {
        return subjectMapping;
    }

    public void setSubjectMapping(Map<Long, Long> subjectMapping) {
        this.subjectMapping = subjectMapping;
    }

    public List<Long> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<Long> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public UserRole getUserRole() {
        if (userRole == null) {
            return UserRole.USER;
        }
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

}
