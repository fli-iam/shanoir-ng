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

package org.shanoir.ng.subjectstudy;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.shanoir.ng.study.Study;
import org.shanoir.ng.subject.Subject;
import org.shanoir.ng.subject.SubjectType;

/**
 * Relation between the subjects and the studies.
 * 
 * @author msimon
 *
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "study_id", "subject_id" }, name = "study_subject_idx") })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class SubjectStudy extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 734032331139342460L;

	/** true if the subject is physically involved in the study. */
	private boolean physicallyInvolved;

	/** Study. */
	@ManyToOne
	@JoinColumn(name = "study_id")
	@NotNull
	private Study study;

	/** Subject. */
	@ManyToOne
	@JoinColumn(name = "subject_id", updatable = true, insertable = true)
	@NotNull
	private Subject subject;

	/** Identifier of the subject inside the study. */
	private String subjectStudyIdentifier;

	/** Subject type. */
	private Integer subjectType;

	/**
	 * @return the physicallyInvolved
	 */
	public boolean isPhysicallyInvolved() {
		return physicallyInvolved;
	}

	/**
	 * @param physicallyInvolved
	 *            the physicallyInvolved to set
	 */
	public void setPhysicallyInvolved(boolean physicallyInvolved) {
		this.physicallyInvolved = physicallyInvolved;
	}

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study
	 *            the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

	/**
	 * @return the subject
	 */
	public Subject getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	/**
	 * @return the subjectStudyIdentifier
	 */
	public String getSubjectStudyIdentifier() {
		return subjectStudyIdentifier;
	}

	/**
	 * @param subjectStudyIdentifier
	 *            the subjectStudyIdentifier to set
	 */
	public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
		this.subjectStudyIdentifier = subjectStudyIdentifier;
	}

	/**
	 * @return the subjectType
	 */
	public SubjectType getSubjectType() {
		return SubjectType.getType(subjectType);
	}

	/**
	 * @param subjectType
	 *            the subjectType to set
	 */
	public void setSubjectType(SubjectType subjectType) {
		if (subjectType == null) {
			this.subjectType = null;
		} else {
			this.subjectType = subjectType.getId();
		}
	}

}
