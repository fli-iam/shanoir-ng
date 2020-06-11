package org.shanoir.ng.score;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.shanoir.ng.examination.model.CodedVariable;
import org.shanoir.ng.shared.core.model.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The Class ScaleItem.
 *
 * @author Jcome
 * @version $Revision: 1.3 $
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ScaleItem extends AbstractEntity {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5995587676825866034L;

	/** The codedVariable. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "coded_variable_id")
	@JsonIgnore
	private CodedVariable codedVariable;

	/** The corresponding number. */
	private Float correspondingNumber;

	/**
	 * The qualitative score code. Might exceed 255 characters so the mapping
	 * uses a blob.
	 */
	@Lob
	private String qualitativeScaleItem;

	/** The quantitative score code. */
	private String quantitativeScaleItem;

	/** Coded variable value type. */
	private String refScaleItemType;

	/**
	 * Gets the instrument variable.
	 *
	 * @return the codedVariable
	 */
	public CodedVariable getCodedVariable() {
		return codedVariable;
	}

	/**
	 * Gets the corresponding number.
	 *
	 * @return the correspondingNumber
	 */
	public Float getCorrespondingNumber() {
		return correspondingNumber;
	}

	/**
	 * Gets the qualitative scale item.
	 *
	 * @return the quantitativeScaleItem
	 */
	public String getQualitativeScaleItem() {
		return qualitativeScaleItem;
	}

	/**
	 * Gets the quantitative scale item.
	 *
	 * @return the quantitativeScaleItem
	 */
	public String getQuantitativeScaleItem() {
		return quantitativeScaleItem;
	}

	/**
	 * Gets the ref scale item type.
	 *
	 * @return the refScaleItemType
	 */
	public String getRefScaleItemType() {
		return refScaleItemType;
	}

	/**
	 * Sets the instrument variable.
	 *
	 * @param codedVariable
	 *            the codedVariable to set
	 */
	public void setCodedVariable(final CodedVariable codedVariable) {
		this.codedVariable = codedVariable;
	}

	/**
	 * Sets the corresponding number.
	 *
	 * @param correspondingNumber
	 *            the correspondingNumber to set
	 */
	public void setCorrespondingNumber(final Float correspondingNumber) {
		this.correspondingNumber = correspondingNumber;
	}

	/**
	 * Sets the qualitative scale item.
	 *
	 * @param qualitativeScaleItem
	 *            the qualitativeScaleItem to set
	 */
	public void setQualitativeScaleItem(final String qualitativeScaleItem) {
		this.qualitativeScaleItem = qualitativeScaleItem;
	}

	/**
	 * Sets the quantitative score code.
	 *
	 * @param quantitativeScaleItem
	 *            the quantitativeScaleItem to set
	 */
	public void setQuantitativeScaleItem(final String quantitativeScaleItem) {
		this.quantitativeScaleItem = quantitativeScaleItem;
	}

	/**
	 * Sets the ref scale item type.
	 *
	 * @param refScaleItemType
	 *            the refScaleItemType to set
	 */
	public void setRefScaleItemType(final String refScaleItemType) {
		this.refScaleItemType = refScaleItemType;
	}
}
