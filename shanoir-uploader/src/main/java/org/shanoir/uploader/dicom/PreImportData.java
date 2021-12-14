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
