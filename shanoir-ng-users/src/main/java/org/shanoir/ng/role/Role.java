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

package org.shanoir.ng.role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.model.AbstractGenericItem;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Role
 */
@Entity
public class Role extends AbstractGenericItem implements GrantedAuthority {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -8021102195810091679L;

	@NotNull
	private int accessLevel;

	@NotBlank
	@Column(unique = true)
	private String displayName;

	@NotBlank
	@Column(unique = true)
	private String name;

	/**
	 * @return the accessLevel
	 */
	@JsonIgnore
	public int getAccessLevel() {
		return accessLevel;
	}

	/**
	 * @param accessLevel the accessLevel to set
	 */
	public void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the name
	 */
	@JsonIgnore
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	@JsonIgnore
	public String getAuthority() {
		return name;
	}

}
