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

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;


@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyCardRule extends AbstractEntity {

	private static final long serialVersionUID = 6708188853533591193L;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="rule_id")
	private List<StudyCardAssignment> assignments;
	
@	OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="rule_id")
	private List<StudyCardCondition> conditions;
	

	public List<StudyCardAssignment> getAssignments() {
		return assignments;
	}

	public void setAssignments(List<StudyCardAssignment> assignments) {
		this.assignments = assignments;
	}

	public List<StudyCardCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<StudyCardCondition> conditions) {
		this.conditions = conditions;
	}
}
