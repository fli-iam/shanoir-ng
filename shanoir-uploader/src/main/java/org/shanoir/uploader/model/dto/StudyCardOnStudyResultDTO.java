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
