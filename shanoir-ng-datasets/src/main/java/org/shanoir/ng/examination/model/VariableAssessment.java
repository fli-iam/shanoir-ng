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

package org.shanoir.ng.examination.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.shanoir.ng.score.Score;
import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Variable assessment.
 * 
 * @author ifakhfakh, JCome
 *
 */
@Entity
public class VariableAssessment extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 8323867968700523066L;

	/** Instrument Based Assessment. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument_based_assessment_id", nullable = false, updatable = true)
	@JsonIgnore
	private InstrumentBasedAssessment instrumentBasedAssessment;

	/** The variable. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument_variable_id", nullable = false, updatable = true)
	@JsonIgnore
	private InstrumentVariable instrumentVariable;

	/** The score list. */
	@OneToMany(mappedBy = "variableAssessment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Score> scoreList;

	/**
	 * @return the instrumentBasedAssessment
	 */
	public InstrumentBasedAssessment getInstrumentBasedAssessment() {
		return instrumentBasedAssessment;
	}

	/**
	 * @param instrumentBasedAssessment
	 *            the instrumentBasedAssessment to set
	 */
	public void setInstrumentBasedAssessment(InstrumentBasedAssessment instrumentBasedAssessment) {
		this.instrumentBasedAssessment = instrumentBasedAssessment;
	}

	/**
	 * @return the instrumentVariable
	 */
	public InstrumentVariable getInstrumentVariable() {
		return instrumentVariable;
	}

	/**
	 * @param instrumentVariable
	 *            the instrumentVariable to set
	 */
	public void setInstrumentVariable(InstrumentVariable instrumentVariable) {
		this.instrumentVariable = instrumentVariable;
	}

	/**
	 * @return the scoreList
	 */
	public List<Score> getScoreList() {
		return scoreList;
	}

	/**
	 * @param scoreList
	 *            the scoreList to set
	 */
	public void setScoreList(List<Score> scoreList) {
		this.scoreList = scoreList;
	}

}
