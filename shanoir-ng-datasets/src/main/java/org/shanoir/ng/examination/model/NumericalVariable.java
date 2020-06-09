package org.shanoir.ng.examination.model;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * The Class NumericalVariable.
 * @author JCome
 */
@Entity
@PrimaryKeyJoinColumn(name = "instrument_variable_id")
@Table(name = "numerical_variable")
public class NumericalVariable extends InstrumentVariable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4974109889862891260L;

	/** Maximum score value of this variable. */
	private Float maxScoreValue;

	/** Minimum score value of this variable. */
	private Float minScoreValue;

	/**
	 * Gets the max score value.
	 *
	 * @return the maxScoreValue
	 */
	public Float getMaxScoreValue() {
		return maxScoreValue;
	}

	/**
	 * Gets the min score value.
	 *
	 * @return the minScoreValue
	 */
	public Float getMinScoreValue() {
		return minScoreValue;
	}

	/**
	 * Sets the max score value.
	 *
	 * @param maxScoreValue
	 *            the maxScoreValue to set
	 */
	public void setMaxScoreValue(final Float maxScoreValue) {
		this.maxScoreValue = maxScoreValue;
	}

	/**
	 * Sets the min score value.
	 *
	 * @param minScoreValue
	 *            the minScoreValue to set
	 */
	public void setMinScoreValue(final Float minScoreValue) {
		this.minScoreValue = minScoreValue;
	}

}
