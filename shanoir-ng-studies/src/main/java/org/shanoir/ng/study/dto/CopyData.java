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
package org.shanoir.ng.study.dto;

import java.util.List;

public class CopyData {

    public static class SubjectCopy {
        private Long id;
        private String newName;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNewName() {
            return newName;
        }

        public void setNewName(String newName) {
            this.newName = newName;
        }
    }

    private List<Long> datasetIds;
    private List<SubjectCopy> subjects;
    private List<Long> centerIds;
    private Long targetStudyId;

    public List<Long> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<Long> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public List<SubjectCopy> getSubjects() {
        return subjects;
    }

    public List<Long> getSubjectIds() {
        if (subjects == null) {
            return null;
        }
        return subjects.stream().map(SubjectCopy::getId).toList();
    }

    public void setSubjects(List<SubjectCopy> subjects) {
        this.subjects = subjects;
    }

    public List<Long> getCenterIds() {
        return centerIds;
    }

    public void setCenterIds(List<Long> centerIds) {
        this.centerIds = centerIds;
    }

    public Long getTargetStudyId() {
        return targetStudyId;
    }

    public void setTargetStudyId(Long targetStudyId) {
        this.targetStudyId = targetStudyId;
    }
}
