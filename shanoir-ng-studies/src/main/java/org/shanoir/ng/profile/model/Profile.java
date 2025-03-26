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

package org.shanoir.ng.profile.model;


import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * Profile
 *
 * @author pdauvergne
 *
 */
@Entity
@JsonPropertyOrder({ "id", "profileName" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class Profile extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 8829401300909105525L;

	@Column(unique = true)
	@Unique
	private String profileName;

	public Profile() {
	}

	public Profile(String profileName) {
		this.profileName = profileName;
	}

	/**
	 * @return the name
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * @param profileName
	 *            the name to set
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

}
