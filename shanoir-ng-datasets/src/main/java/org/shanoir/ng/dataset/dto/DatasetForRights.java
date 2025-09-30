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

package org.shanoir.ng.dataset.dto;

import java.util.HashSet;
import java.util.Set;


public class DatasetForRights implements DatasetForRightsProjection {

    private Long id;

    private Long studyId;

    private Long centerId;

    private Set<Long> relatedStudiesIds;

    public DatasetForRights(Long id, Long centerId, Long studyId, Set<Long> relatedStudiesIds) {
        this.centerId = centerId;
        this.id = id;
        this.studyId = studyId;
        this.relatedStudiesIds = relatedStudiesIds != null ? relatedStudiesIds : new HashSet<>();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    @Override
    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    @Override
    public Set<Long> getRelatedStudiesIds() {
        return relatedStudiesIds;
    }

    public void setRelatedStudiesIds(Set<Long> relatedStudiesIds) {
        this.relatedStudiesIds = relatedStudiesIds;
    }

    public Set<Long> getAllStudiesIds() {
        Set<Long> all = new HashSet<>(relatedStudiesIds);
        all.add(studyId);
        return all;
    }
}
