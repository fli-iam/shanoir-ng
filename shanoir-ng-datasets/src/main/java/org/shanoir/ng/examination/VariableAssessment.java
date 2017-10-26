package org.shanoir.ng.examination;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.shanoir.ng.score.Score;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Variable assessment.
 * 
 * @author ifakhfakh
 *
 */
@Entity
public class VariableAssessment extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 8323867968700523066L;

	/** Instrument Based Assessment. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument_based_assessment_id", nullable = false, updatable = true)
	private InstrumentBasedAssessment instrumentBasedAssessment;

	/** The variable. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument_variable_id", nullable = false, updatable = true)
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
