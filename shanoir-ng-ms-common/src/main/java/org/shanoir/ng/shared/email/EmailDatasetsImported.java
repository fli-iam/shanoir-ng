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

import java.util.Map;

/**
 * This class represents an EMAIL to be sent after datasets have been imported.
 *
 * @author JCD, mkain
 *
 */
public class EmailDatasetsImported extends EmailBase {

    private Map<Long, String> datasets;

    private String subjectName;

    private String examinationId;

    private String examDate;

    private String studyCard;

    /**
     * @return the datasets
     */
    public Map<Long, String> getDatasets() {
        return datasets;
    }

    /**
     * @param datasets the datasets to set
     */
    public void setDatasets(Map<Long, String>  datasets) {
        this.datasets = datasets;
    }

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
     * @return the examDate
     */
    public String getExamDate() {
        return examDate;
    }

    /**
     * @param examDate the examDate to set
     */
    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    /**
     * @return the studyCard
     */
    public String getStudyCard() {
        return studyCard;
    }

    /**
     * @param studyCard the studyCard to set
     */
    public void setStudyCard(String studyCard) {
        this.studyCard = studyCard;
    }

}
