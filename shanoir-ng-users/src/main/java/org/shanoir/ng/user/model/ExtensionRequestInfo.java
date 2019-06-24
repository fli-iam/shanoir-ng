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
