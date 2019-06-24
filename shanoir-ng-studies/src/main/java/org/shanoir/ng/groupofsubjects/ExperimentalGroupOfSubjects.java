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

package org.shanoir.ng.groupofsubjects;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.study.model.Study;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Experimental group of subjects.
 * 
 * @author msimon
 *
 */
@Entity
@DiscriminatorValue("EXPERIMENTAL")
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
@JsonIdentityInfo(scope=ExperimentalGroupOfSubjects.class , generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class ExperimentalGroupOfSubjects extends GroupOfSubjects {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -554912356059391766L;

	/** Study. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "study_id")
	@NotNull
	private Study study;

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

}
