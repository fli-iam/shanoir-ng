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

public class StudyCardOnStudyResultDTO {

    private String subjectName;

    private String examinationDate;

    private String examinationComment;

    private String resultExaminationLevel;

    private String resultAcquisitionLevel;

    private String resultDatasetLevel;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(String examinationDate) {
        this.examinationDate = examinationDate;
    }

    public String getExaminationComment() {
        return examinationComment;
    }

    public void setExaminationComment(String examinationComment) {
        this.examinationComment = examinationComment;
    }

    public String getResultExaminationLevel() {
        return resultExaminationLevel;
    }

    public void setResultExaminationLevel(String resultExaminationLevel) {
        this.resultExaminationLevel = resultExaminationLevel;
    }

    public String getResultAcquisitionLevel() {
        return resultAcquisitionLevel;
    }

    public void setResultAcquisitionLevel(String resultAcquisitionLevel) {
        this.resultAcquisitionLevel = resultAcquisitionLevel;
    }

    public String getResultDatasetLevel() {
        return resultDatasetLevel;
    }

    public void setResultDatasetLevel(String resultDatasetLevel) {
        this.resultDatasetLevel = resultDatasetLevel;
    }

}
