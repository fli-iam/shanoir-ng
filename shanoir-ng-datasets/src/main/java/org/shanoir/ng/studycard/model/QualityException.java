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

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;

/**
 * Shanoir Quality Check Exception.
 */
public class QualityException extends Exception {

    private Examination examination;

    private QualityCardResult qualityResult;


    public QualityException(Examination examination, QualityCardResult qualityResult) {
        super();
        this.examination = examination;
        this.qualityResult = qualityResult;
    }

	public QualityException(Examination examination, QualityCardResult qualityResult, Throwable cause) {
		super(cause);
		this.examination = examination;
		this.qualityResult = qualityResult;
	}


    public Examination getExamination() {
        return examination;
    }

    public void setExamination(Examination examination) {
        this.examination = examination;
    }

    public QualityCardResult getQualityResult() {
        return qualityResult;
    }

    public void setQualityResult(QualityCardResult qualityResult) {
        this.qualityResult = qualityResult;
    }

    public String buildErrorMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Study : ")
            .append(this.getExamination().getStudy().getName())
            .append(" (").append(this.getExamination().getStudy().getId()).append(")");
        sb.append("\n");
        sb.append("Subject : ")
            .append(this.getExamination().getSubject().getName())
            .append(" (").append(this.getExamination().getSubject().getId()).append(")");
        sb.append("\n");
        sb.append("Examination : ")
            .append(this.getExamination().getComment())
            .append(" (").append(this.getExamination().getId()).append(")");
        sb.append("\n");
        sb.append("Examination : ");
        for (QualityCardResultEntry qcResult : this.getQualityResult()) {
            sb.append("\n- ");
            sb.append(qcResult.getMessage());
        }
        return sb.toString();
    }
}
