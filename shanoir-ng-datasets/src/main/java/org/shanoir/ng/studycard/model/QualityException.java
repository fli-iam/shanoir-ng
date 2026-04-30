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

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;

/**
 * Shanoir Quality Check Exception.
 */
public class QualityException extends Exception {

    private DatasetAcquisition datasetAcquisition;

    private QualityCardResult qualityResult;


    public QualityException(DatasetAcquisition datasetAcquisition, QualityCardResult qualityResult, Throwable cause) {
        super(cause);
        this.datasetAcquisition = datasetAcquisition;
        this.qualityResult = qualityResult;
    }

    public DatasetAcquisition getDatasetAcquisition() {
        return datasetAcquisition;
    }

    public void setDatasetAcquisition(DatasetAcquisition datasetAcquisition) {
        this.datasetAcquisition = datasetAcquisition;
    }

    public QualityCardResult getQualityResult() {
        return qualityResult;
    }

    public void setQualityResult(QualityCardResult qualityResult) {
        this.qualityResult = qualityResult;
    }

    public String buildErrorMessage() { // TODO : improve error messages
        StringBuilder sb = new StringBuilder();
        if (this.getDatasetAcquisition() == null) {
            sb.append("Quality check failed because none of the dataset acquisitions passed the quality check.\n");
        } else {
            sb.append("Study : ")
                .append(this.getDatasetAcquisition().getExamination().getStudy().getName())
                .append(" (").append(this.getDatasetAcquisition().getExamination().getStudy().getId()).append(")");
            sb.append("\n");
            sb.append("Subject : ")
                .append(this.getDatasetAcquisition().getExamination().getSubject().getName())
                .append(" (").append(this.getDatasetAcquisition().getExamination().getSubject().getId()).append(")");
            sb.append("\n");
            sb.append("Examination : ")
                .append(this.getDatasetAcquisition().getExamination().getComment())
                .append(" (").append(this.getDatasetAcquisition().getExamination().getId()).append(")");
            sb.append("\n");
            sb.append("Dataset Acquisition : ");
        }
        for (QualityCardResultEntry qcResult : this.getQualityResult()) {
            sb.append("\n- ");
            sb.append(qcResult.getMessage());
        }
        return sb.toString();
    }
}
