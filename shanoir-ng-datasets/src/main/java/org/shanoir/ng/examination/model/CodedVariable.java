package org.shanoir.ng.examination.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.shanoir.ng.score.ScaleItem;

/**
 * The Class NumericalVariable.
 *
 * @author JCome
 */
@Entity
@PrimaryKeyJoinColumn(name = "instrument_variable_id")
@Table(name = "coded_variable")
public class CodedVariable extends InstrumentVariable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -28168171333546302L;

	/** Minimum value. */
	@OneToOne
	@JoinColumn(name = "max_scale_item_id")
	private ScaleItem maxScaleItem;

	/** Minimum value. */
	@OneToOne
	@JoinColumn(name = "min_scale_item_id")
	private ScaleItem minScaleItem;

	/** The ref score code list. */
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "coded_variable_id")
	private List<ScaleItem> scaleItemList = new ArrayList<>(0);

	/**
	 * Gets the max scale item.
	 *
	 * @return the maxScaleItem
	 */
	public ScaleItem getMaxScaleItem() {
		return maxScaleItem;
	}

	/**
	 * Gets the min scale item.
	 *
	 * @return the minScaleItem
	 */
	public ScaleItem getMinScaleItem() {
		return minScaleItem;
	}

	/**
	 * Gets the scale item list.
	 *
	 * @return the scaleItemList
	 */
	public List<ScaleItem> getScaleItemList() {
		return scaleItemList;
	}

	/**
	 * Sets the max scale item.
	 *
	 * @param maxScaleItem
	 *            the maxScaleItem to set
	 */
	public void setMaxScaleItem(final ScaleItem maxScaleItem) {
		this.maxScaleItem = maxScaleItem;
	}

	/**
	 * Sets the min scale item.
	 *
	 * @param minScaleItem
	 *            the minScaleItem to set
	 */
	public void setMinScaleItem(final ScaleItem minScaleItem) {
		this.minScaleItem = minScaleItem;
	}

	/**
	 * Sets the scale Item List.
	 *
	 * @param refScaleItemList
	 *            the refScaleItemList to set
	 */
	public void setScaleItemList(final List<ScaleItem> refScaleItemList) {
		this.scaleItemList = refScaleItemList;
	}
}
