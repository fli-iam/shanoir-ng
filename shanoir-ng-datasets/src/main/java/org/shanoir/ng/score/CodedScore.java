package org.shanoir.ng.score;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * The Class Score.
 *
 * @author JCome
 *
 */
@Entity
@PrimaryKeyJoinColumn(name = "score_id")
@Table(name = "coded_score")
public class CodedScore extends Score {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 931538633792918610L;

	/** Ref Score code. */
	@ManyToOne(fetch = FetchType.LAZY)
	@NotNull
	@JoinColumn(name = "scale_item_id")
	private ScaleItem scaleItem;

	/**
	 * Gets the scale item.
	 *
	 * @return the scaleItem
	 */
	public ScaleItem getScaleItem() {
		return scaleItem;
	}

	/**
	 * Sets the ref score code.
	 *
	 * @param scaleItem
	 *            the scaleItem to set
	 */
	public void setScaleItem(ScaleItem scaleItem) {
		this.scaleItem = scaleItem;
	}

}
