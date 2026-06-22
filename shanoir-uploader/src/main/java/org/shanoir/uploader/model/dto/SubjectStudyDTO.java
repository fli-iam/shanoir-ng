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

package org.shanoir.uploader.model.dto;

public class SubjectStudyDTO {

    private Long id;

    private Long studyId;

    private boolean physicallyInvolved;

    private String subjectStudyIdentifier;

    private String subjectType;

    public SubjectStudyDTO(Long id, Long studyId, boolean physicallyInvolved, String subjectStudyIdentifier,
            String subjectType) {
        super();
        this.id = id;
        this.studyId = studyId;
        this.physicallyInvolved = physicallyInvolved;
        this.subjectStudyIdentifier = subjectStudyIdentifier;
        this.subjectType = subjectType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isPhysicallyInvolved() {
        return physicallyInvolved;
    }

    public void setPhysicallyInvolved(boolean physicallyInvolved) {
        this.physicallyInvolved = physicallyInvolved;
    }

    public String getSubjectStudyIdentifier() {
        return subjectStudyIdentifier;
    }

    public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
        this.subjectStudyIdentifier = subjectStudyIdentifier;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

}
