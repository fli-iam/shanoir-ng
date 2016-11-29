package org.shanoir.ng.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;

/**
 * Role
 */
@Entity
public class Role {

	@Id
	private Long id = null;

	@NotBlank
	private String name = null;

	@NotNull
	private Integer accessLevel = null;


	/**
	 * Get id
	 *
	 * @return id
	 **/
	@ApiModelProperty(required = true, value = "")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * Get name
	 *
	 * @return name
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Get accessLevel
	 *
	 * @return accessLevel
	 **/
	@ApiModelProperty(required = true, value = "")
	public Integer getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(Integer accessLevel) {
		this.accessLevel = accessLevel;
	}

}
