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

package org.shanoir.ng.studycenter;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.study.model.Study;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Link between studies and centers.
 * 
 * @author msimon
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class StudyCenter extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 1007750133610651645L;

	/** Center. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "center_id", nullable=false)
	private Center center;

	/** The study. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id", nullable=false)
	private Study study;
	
	/** The subject name prefix associated to the center. */
	private String subjectNamePrefix;
	
	/**
	 * @return the center
	 */
	public Center getCenter() {
		return center;
	}

	/**
	 * @param center
	 *            the center to set
	 */
	public void setCenter(Center center) {
		this.center = center;
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

	public String getSubjectNamePrefix() {
		return subjectNamePrefix;
	}

	public void setSubjectNamePrefix(String subjectNamePrefix) {
		this.subjectNamePrefix = subjectNamePrefix;
	}

}
