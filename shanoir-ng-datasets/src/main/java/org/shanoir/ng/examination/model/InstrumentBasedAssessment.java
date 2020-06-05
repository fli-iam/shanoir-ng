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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Instrument based assessment.
 * 
 * @author ifakhfakh, JCome
 *
 */
@Entity
public class InstrumentBasedAssessment extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2356266338557542044L;

	/** Related Examination. */
	@ManyToOne
	@JoinColumn(name = "examination_id", updatable = true, nullable = false)
	@JsonIgnore
	private Examination examination;

	/** Instrument. */
	@ManyToOne
	@JoinColumn(name = "instrument_id", updatable = true, nullable = false)
	private Instrument instrument;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrumentBasedAssessment")
	private List<VariableAssessment> variableAssessmentList;

	/**
	 * @return the examination
	 */
	public Examination getExamination() {
		return examination;
	}

	/**
	 * @param examination
	 *            the examination to set
	 */
	public void setExamination(Examination examination) {
		this.examination = examination;
	}

	/**
	 * @return the instrument
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * @param instrument
	 *            the instrument to set
	 */
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	/**
	 * @return the variableAssessmentList
	 */
	public List<VariableAssessment> getVariableAssessmentList() {
		return variableAssessmentList;
	}

	/**
	 * @param variableAssessmentList
	 *            the variableAssessmentList to set
	 */
	public void setVariableAssessmentList(List<VariableAssessment> variableAssessmentList) {
		this.variableAssessmentList = variableAssessmentList;
	}

}
