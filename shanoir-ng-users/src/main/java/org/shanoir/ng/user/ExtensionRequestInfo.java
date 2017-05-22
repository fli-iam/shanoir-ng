package org.shanoir.ng.user;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * Extension request info.
 * 
 * @author msimon
 *
 */
@Embeddable
public class ExtensionRequestInfo {

	@NotNull
	private Date extensionDate;

	@NotNull
	private String extensionMotivation;

	/**
	 * @return the extensionDate
	 */
	public Date getExtensionDate() {
		return extensionDate;
	}

	/**
	 * @param extensionDate
	 *            the extensionDate to set
	 */
	public void setExtensionDate(Date extensionDate) {
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
