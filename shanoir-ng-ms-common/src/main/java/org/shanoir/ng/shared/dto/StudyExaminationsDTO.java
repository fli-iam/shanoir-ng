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

package org.shanoir.ng.shared.dto;

import java.util.ArrayList;
import java.util.List;

public class StudyExaminationsDTO {

    private Long studyId;
    private List<StudyExaminationDTO> examinations = new ArrayList<>();

    public StudyExaminationsDTO(Long studyId) {
        this.studyId = studyId;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public List<StudyExaminationDTO> getExaminations() {
        return examinations;
    }

    public void setExaminations(List<StudyExaminationDTO> examinations) {
        this.examinations = examinations;
    }

    public void addExam(Long examinationId, Long centerId, Long subjectId) {
        StudyExaminationDTO exam = new StudyExaminationDTO();
        exam.setExaminationId(examinationId);
        exam.setCenterId(centerId);
        exam.setSubjectId(subjectId);
        examinations.add(exam);
    }

    public class StudyExaminationDTO {

        private Long examinationId;
        private Long centerId;
        private Long subjectId;

        public Long getExaminationId() {
            return examinationId;
        }

        public void setExaminationId(Long examinationId) {
            this.examinationId = examinationId;
        }

        public Long getCenterId() {
            return centerId;
        }

        public void setCenterId(Long centerId) {
            this.centerId = centerId;
        }

        public Long getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Long subjectId) {
            this.subjectId = subjectId;
        }
    }
}
