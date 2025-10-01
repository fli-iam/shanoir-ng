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

package org.shanoir.ng.studycard.model;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

public class ExaminationData {
    
    private String subjectName;
    
    @LocalDateAnnotations
    private LocalDate examinationDate;
    
    private String examinationComment;
    
    private List<DatasetAcquisition> datasetAcquisitions;
    
    private Long subjectId;

    private Long studyId;

    public ExaminationData() { }

    public ExaminationData(Examination examination) {
        if (examination == null) throw new IllegalArgumentException("examination can't be null");
        if (examination.getDatasetAcquisitions() == null) throw new IllegalArgumentException("examination acquisitions can't be null");
        if (examination.getStudy() == null) throw new IllegalArgumentException("study can't be null");
        if (examination.getSubject() == null) throw new IllegalArgumentException("subject can't be null");
        // Keep only MR acquisitions
        // List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions().stream().filter(a -> a instanceof MrDatasetAcquisition).collect(Collectors.toList());
        setStudyId(examination.getStudy().getId());
        setDatasetAcquisitions(examination.getDatasetAcquisitions());
        setExaminationComment(examination.getComment());
        setExaminationDate(examination.getExaminationDate());
        setSubjectName(examination.getSubject().getName());
        setSubjectId(examination.getSubject().getId());
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public LocalDate getExaminationDate() {
        return examinationDate;
    }

    public void setExaminationDate(LocalDate examinationDate) {
        this.examinationDate = examinationDate;
    }

    public String getExaminationComment() {
        return examinationComment;
    }

    public void setExaminationComment(String examinationComment) {
        this.examinationComment = examinationComment;
    }

    public List<DatasetAcquisition> getDatasetAcquisitions() {
        return datasetAcquisitions;
    }

    public void setDatasetAcquisitions(List<DatasetAcquisition> datasetAcquisitions) {
        this.datasetAcquisitions = datasetAcquisitions;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

}
