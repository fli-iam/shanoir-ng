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

package org.shanoir.ng.studyexamination;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subject.model.Subject;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * This class is the link between a study and an examination.
 * It also supports linked center and subject.
 * @author jcome
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class StudyExamination extends AbstractEntity {

    private static final long serialVersionUID = -6040639164236575228L;

    private Long examinationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "center_id")
    private Center center;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    public StudyExamination() {
        // default constructor
    }

    public StudyExamination(Long examinationId, Study study, Center center, Subject subject) {
        super();
        this.examinationId = examinationId;
        this.study = study;
        this.center = center;
        this.subject = subject;
    }

    /**
     * @return the examinationId
     */
    public Long getExaminationId() {
        return examinationId;
    }

    /**
     * @param examinationId the examinationId to set
     */
    public void setExaminationId(Long examinationId) {
        this.examinationId = examinationId;
    }

    /**
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    /**
     * @return the center
     */
    public Center getCenter() {
        return center;
    }

    /**
     * @param center the center to set
     */
    public void setCenter(Center center) {
        this.center = center;
    }

    /**
     * @return the subject
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

}
