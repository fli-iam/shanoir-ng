package org.shanoir.ng.user.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

/**
 * Extension request info.
 * 
 * @author msimon
 *
 */
@Embeddable
public class ExtensionRequestInfo implements Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -6296721709358679698L;

	@NotNull
	@LocalDateAnnotations
	private LocalDate extensionDate;

	@NotNull
	private String extensionMotivation;

	/**
	 * @return the extensionDate
	 */
	public LocalDate getExtensionDate() {
		return extensionDate;
	}

	/**
	 * @param extensionDate
	 *            the extensionDate to set
	 */
	public void setExtensionDate(LocalDate extensionDate) {
		this.extensionDate = extensionDate;
	}

	/**
	 * @return the extensionMotivation
	 */
	public String getExtensionMotivation() {
		return extensionMotivation;
	}

	/**
	 * @param extensionMotivation
	 *            the extensionMotivation to set
	 */
	public void setExtensionMotivation(String extensionMotivation) {
		this.extensionMotivation = extensionMotivation;
	}

}
