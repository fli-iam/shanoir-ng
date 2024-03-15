package org.shanoir.uploader.model;

import org.shanoir.uploader.model.rest.Subject;

import java.util.List;

public class FolderImport {

    private List<ExaminationImport> examinationImports;

    private org.shanoir.uploader.model.rest.Study study;
    private org.shanoir.uploader.model.rest.StudyCard studyCard;

    private List<Subject> listOfSubjectsForStudy;

    public org.shanoir.uploader.model.rest.Study getStudy() {
        return study;
    }

    public void setStudy(org.shanoir.uploader.model.rest.Study study) {
        this.study = study;
    }

    public org.shanoir.uploader.model.rest.StudyCard getStudyCard() {
        return studyCard;
    }

    public void setStudyCard(org.shanoir.uploader.model.rest.StudyCard studyCard) {
        this.studyCard = studyCard;
    }

    public List<ExaminationImport> getExaminationImports() {
        return examinationImports;
    }

    public void setExaminationImports(List<ExaminationImport> examinationImports) {
        this.examinationImports = examinationImports;
    }

    public List<Subject> getListOfSubjectsForStudy() {
        return listOfSubjectsForStudy;
    }

    public void setListOfSubjectsForStudy(List<Subject> listOfSubjectsForStudy) {
        this.listOfSubjectsForStudy = listOfSubjectsForStudy;
    }
}
