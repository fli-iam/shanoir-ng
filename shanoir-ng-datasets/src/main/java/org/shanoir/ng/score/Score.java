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

package org.shanoir.ng.score;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;

import org.shanoir.ng.examination.VariableAssessment;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Score.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "variableAssessment" })
public class Score extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5995692961509079060L;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variable_assessment_id", nullable = false, updatable = true)
	private VariableAssessment variableAssessment;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "score/" + getId());
	}

	public VariableAssessment getVariableAssessment() {
		return variableAssessment;
	}

	public void setVariableAssessment(VariableAssessment variableAssessment) {
		this.variableAssessment = variableAssessment;
	}

}
