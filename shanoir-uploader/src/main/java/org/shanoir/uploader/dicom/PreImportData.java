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

package org.shanoir.uploader.dicom;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "autoImportEnable", "study", "studycard", "subjectId", "examinationId" })
public class PreImportData {

	private boolean autoImportEnable;

	private Study study;

	private StudyCard studycard;

	private Long subjectId;

	private Long examinationId;

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public StudyCard getStudycard() {
		return studycard;
	}

	public void setStudycard(StudyCard studycard) {
		this.studycard = studycard;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public boolean isAutoImportEnable() {
		return autoImportEnable;
	}

	public void setAutoImportEnable(boolean autoImportEnable) {
		this.autoImportEnable = autoImportEnable;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

}
