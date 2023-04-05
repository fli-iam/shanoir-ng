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

package org.shanoir.ng.studycard.model.rule;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import org.dcm4che3.data.Attributes;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.studycard.model.assignment.StudyCardAssignment;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
@DiscriminatorColumn(name="scope", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "scope")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DatasetRule.class, name = "Dataset"),
    @JsonSubTypes.Type(value = DatasetAcquisitionRule.class, name = "DatasetAcquisition") })
public abstract class StudyCardRule<T> extends AbstractEntity {

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="rule_id")
	private List<StudyCardAssignment<?>> assignments;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	// there is a join table because a rule_id fk would lead to an ambiguity and bugs 
    // because it could refer to a study card or quality card rule
	@JoinTable(name="study_card_condition_join", joinColumns = {@JoinColumn(name = "study_card_rule_id")}, inverseJoinColumns = {@JoinColumn(name = "condition_id")})
	private List<StudyCardCondition> conditions;
	
	public List<StudyCardAssignment<?>> getAssignments() {
		return assignments;
	}

	public void setAssignments(List<StudyCardAssignment<?>> assignments) {
		this.assignments = assignments;
	}

	public List<StudyCardCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<StudyCardCondition> conditions) {
		this.conditions = conditions;
	}
	
	abstract void apply(T object, Attributes dicomAttributes);
}
