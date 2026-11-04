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

import java.util.List;

/**
 * This class represents an EMAIL to be sent after extra data files have been
 * added to an examination or a dataset acquisition.
 */
public class EmailExtraDataAdded extends EmailBase {

    private String subjectName;

    private String examinationId;

    private String examDate;

    /** Set when the files were added to a dataset acquisition, null when added to the examination. */
    private String datasetAcquisitionId;

    private List<String> fileNames;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getExaminationId() {
        return examinationId;
    }

    public void setExaminationId(String examinationId) {
        this.examinationId = examinationId;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getDatasetAcquisitionId() {
        return datasetAcquisitionId;
    }

    public void setDatasetAcquisitionId(String datasetAcquisitionId) {
        this.datasetAcquisitionId = datasetAcquisitionId;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

}
