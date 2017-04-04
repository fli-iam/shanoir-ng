package org.shanoir.ng.study;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.shanoir.ng.shared.validation.Unique;

/**
 * Study status.
 * 
 * @author msimon
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class StudyStatus extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 1636061761940084952L;

	/** The label name. */
	@NotBlank
	@Column(unique = true)
	@Unique
	private String labelName;

	/**
	 * Gets the label name.
	 *
	 * @return the label name
	 */
	public String getLabelName() {
		return labelName;
	}

	/**
	 * Sets the label name.
	 *
	 * @param labelName
	 *            the label name
	 */
	public void setLabelName(final String labelName) {
		this.labelName = labelName;
	}

}
