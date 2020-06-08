package org.shanoir.ng.score;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.shanoir.ng.examination.model.ScientificArticle;

/**
 * The Class NumericalScore.
 *
 * @author Jcome
 */
@Entity
@PrimaryKeyJoinColumn(name = "score_id")
@Table(name = "numerical_score")
public class NumericalScore extends Score {

	private static final long serialVersionUID = 1L;

	/** Is Score With Unit Of Measure. */
	private boolean isScoreWithUnitOfMeasure;

	/** Score type. */
	private String refNumericalScoreType;

	/**
	 * Unit of Measure. If null, then it means that this numerical score is
	 * without unit of measure.
	 */
	private String refUnitOfMeasure;

	/** Scientific Article. It must be a score standardisation article. */
	@ManyToOne
	@JoinColumn(name = "scientific_article_id")
	private ScientificArticle scoreStandardisationArticle;

	/** the numerical value. */
	private Float value;

	/**
	 * Gets the ref numerical score type.
	 *
	 * @return the refNumericalScoreType
	 */
	public String getRefNumericalScoreType() {
		return refNumericalScoreType;
	}

	/**
	 * Gets the ref unit of measure.
	 *
	 * @return the refUnitOfMeasure
	 */
	public String getRefUnitOfMeasure() {
		return refUnitOfMeasure;
	}

	/**
	 * Gets the score standardisation article.
	 *
	 * @return the scoreStandardisationArticle
	 */
	public ScientificArticle getScoreStandardisationArticle() {
		return scoreStandardisationArticle;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Float getValue() {
		return value;
	}

	/**
	 * Checks if is score with unit of measure.
	 *
	 * @return the isScoreWithUnitOfMeasure
	 */
	public boolean isScoreWithUnitOfMeasure() {
		return isScoreWithUnitOfMeasure;
	}

	/**
	 * Sets the ref numerical score type.
	 *
	 * @param refNumericalScoreType
	 *            the refNumericalScoreType to set
	 */
	public void setRefNumericalScoreType(final String refNumericalScoreType) {
		this.refNumericalScoreType = refNumericalScoreType;
	}

	/**
	 * Sets the ref unit of measure.
	 *
	 * @param refUnitOfMeasure
	 *            the refUnitOfMeasure to set
	 */
	public void setRefUnitOfMeasure(final String refUnitOfMeasure) {
		this.refUnitOfMeasure = refUnitOfMeasure;
	}

	/**
	 * Sets the score standardisation article.
	 *
	 * @param scoreStandardisationArticle
	 *            the scoreStandardisationArticle to set
	 */
	public void setScoreStandardisationArticle(final ScientificArticle scoreStandardisationArticle) {
		this.scoreStandardisationArticle = scoreStandardisationArticle;
	}

	/**
	 * Sets the score with unit of measure.
	 *
	 * @param isScoreWithUnitOfMeasure
	 *            the isScoreWithUnitOfMeasure to set
	 */
	public void setScoreWithUnitOfMeasure(final boolean isScoreWithUnitOfMeasure) {
		this.isScoreWithUnitOfMeasure = isScoreWithUnitOfMeasure;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the value to set
	 */
	public void setValue(final Float value) {
		this.value = value;
	}
}
