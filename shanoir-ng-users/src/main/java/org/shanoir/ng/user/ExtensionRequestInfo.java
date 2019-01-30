/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.user;

import java.io.Serializable;
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
public class ExtensionRequestInfo implements Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -6296721709358679698L;

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
