package org.shanoir.ng.examination;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Instrument based assessment.
 * 
 * @author ifakhfakh
 *
 */
@Entity
public class InstrumentBasedAssessment extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2356266338557542044L;

	/** Related Examination. */
	@ManyToOne
	@JoinColumn(name = "examination_id", updatable = true, nullable = false)
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
