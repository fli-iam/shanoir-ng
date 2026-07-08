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

package org.shanoir.ng.subject.model;

import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.subjectstudy.SubjectType;
import org.shanoir.ng.study.model.Study;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON carrier for the subjectStudyList property still sent by legacy
 * clients (old ShUp versions) instead of the direct subject.study
 * relation. Not persisted: the subject_study table does not exist
 * anymore, the attributes are mapped onto the subject itself.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacySubjectStudy {

    private boolean physicallyInvolved;

    private Study study;

    private String subjectStudyIdentifier;

    private SubjectType subjectType;

    private QualityTag qualityTag;

    public boolean isPhysicallyInvolved() {
        return physicallyInvolved;
    }

    public void setPhysicallyInvolved(boolean physicallyInvolved) {
        this.physicallyInvolved = physicallyInvolved;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public String getSubjectStudyIdentifier() {
        return subjectStudyIdentifier;
    }

    public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
        this.subjectStudyIdentifier = subjectStudyIdentifier;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public QualityTag getQualityTag() {
        return qualityTag;
    }

    public void setQualityTag(QualityTag qualityTag) {
        this.qualityTag = qualityTag;
    }

}
