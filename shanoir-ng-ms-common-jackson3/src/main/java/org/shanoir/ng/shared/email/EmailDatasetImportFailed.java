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

package org.shanoir.ng.shared.email;

public class EmailDatasetImportFailed extends EmailBase {

    private String errorMessage;

    private String subjectName;

    private String examinationId;

    private String studyCardId;

    /**
     * @return the subjectName
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * @param subjectName the subjectName to set
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    /**
     * @return the examinationId
     */
    public String getExaminationId() {
        return examinationId;
    }

    /**
     * @param examinationId the examinationId to set
     */
    public void setExaminationId(String examinationId) {
        this.examinationId = examinationId;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the studyCardId
     */
    public String getStudyCardId() {
        return studyCardId;
    }

    /**
     * @param studyId the studyCardId to set
     */
    public void setStudyCardId(String studyCardId) {
        this.studyCardId = studyCardId;
    }
}
