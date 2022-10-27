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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyCardCondition extends AbstractEntity {

	/** UID */
	private static final long serialVersionUID = 6708177853555591193L;
	
	private int dicomTagOrField;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name="condition_id")
	private List<StudyCardConditionValue> values;
	
	@NotNull
	private int operation;
	
	public int getDicomTagOrField() {
		return dicomTagOrField;
	}

	public void setDicomTagOrField(int dicomTagOrField) {
		this.dicomTagOrField = dicomTagOrField;
	}

	public Operation getOperation() {
		return Operation.getType(operation);
	}

	public void setOperation(Operation operation) {
		this.operation = operation.getId();
	}

	public List<StudyCardConditionValue> getValues() {
		return values;
	}

	public void setValues(List<StudyCardConditionValue> values) {
		this.values = values;
	}

}
