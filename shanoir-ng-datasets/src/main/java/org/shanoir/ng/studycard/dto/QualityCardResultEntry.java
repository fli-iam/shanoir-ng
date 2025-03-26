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

package org.shanoir.ng.studycard.dto;

import java.time.LocalDate;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.quality.QualityTag;

/**
 * @author mkain
 */
public class QualityCardResultEntry {

	private String subjectName;
	
	@LocalDateAnnotations
    private LocalDate examinationDate;
	
	private String examinationComment;
	
	private String message;
	
	private QualityTag tagSet;

	private boolean failedValid = false; // if tag VALID was to put but conditions failed

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public QualityTag getTagSet() {
        return tagSet;
    }

    public void setTagSet(QualityTag tagSet) {
        this.tagSet = tagSet;
    }

	public boolean isFailedValid() {
		return failedValid;
	}

	public void setFailedValid(boolean failedValid) {
		this.failedValid = failedValid;
	}
}
