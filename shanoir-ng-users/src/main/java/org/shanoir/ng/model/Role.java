package org.shanoir.ng.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Role
 */
@Entity
public class Role implements GrantedAuthority {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -8021102195810091679L;

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	private int accessLevel;

	@NotBlank
	@Column(unique = true)
	private String displayName;

	@NotBlank
	@Column(unique = true)
	private String name;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

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
