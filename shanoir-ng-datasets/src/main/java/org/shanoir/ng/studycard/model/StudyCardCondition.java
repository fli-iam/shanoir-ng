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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyCardCondition extends AbstractEntity {

	/** UID */
	private static final long serialVersionUID = 6708188853533591193L;
	
	@NotNull
	private String dicomTag;
	
	private String dicomValue;
	
	@NotNull
	private Operation operation;
	
	@NotNull
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	private StudyCardRule rule;
	

	public StudyCardRule getRule() {
		return rule;
	}

	public void setRule(StudyCardRule rule) {
		this.rule = rule;
	}

	public String getDicomTag() {
		return dicomTag;
	}

	public void setDicomTag(String dicomTag) {
		this.dicomTag = dicomTag;
	}

	public String getDicomValue() {
		return dicomValue;
	}

	public void setDicomValue(String dicomValue) {
		this.dicomValue = dicomValue;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}
}
