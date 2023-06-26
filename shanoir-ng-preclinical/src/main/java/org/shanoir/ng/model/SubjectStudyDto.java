package org.shanoir.ng.model;

import org.shanoir.ng.shared.core.model.IdName;

public class SubjectStudyDto {

    private IdName subject;

    private IdName study;

    public SubjectStudyDto(IdName subject, IdName study) {
        this.subject = subject;
        this.study = study;
    }

    /**
     * @return the subject
     */
    public IdName getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(IdName subject) {
        this.subject = subject;
    }

    /**
     * @return the study
     */
    public IdName getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(IdName study) {
        this.study = study;
    }


}
