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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public class SubjectIdMapping {

    private Map<Long, Long> mapping = new HashMap<>();

    public SubjectIdMapping() { }

    @JsonCreator
    public SubjectIdMapping(Map<Long, Long> mapping) {
        this.mapping = mapping != null ? mapping : new HashMap<>();
    }

    public void map(Long sourceSubjectId, Long targetSubjectId) {
        mapping.put(sourceSubjectId, targetSubjectId);
    }

    public Long getTargetId(Long sourceSubjectId) {
        return mapping.get(sourceSubjectId);
    }

    public boolean containsSource(Long sourceSubjectId) {
        return mapping.containsKey(sourceSubjectId);
    }

    @JsonValue
    public Map<Long, Long> asMap() {
        return mapping;
    }

    @Override
    public String toString() {
        return "SubjectIdMapping{"
                + "mapping=" + mapping
                + '}';
    }
}
