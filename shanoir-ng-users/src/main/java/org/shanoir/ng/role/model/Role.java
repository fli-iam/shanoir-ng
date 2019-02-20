package org.shanoir.ng.role.model;

import javax.persistence.Column;
import javax.persistence.Entity;

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

	@NotBlank
	@Column(unique = true)
	private String displayName;

	@NotBlank
	@Column(unique = true)
	private String name;


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
