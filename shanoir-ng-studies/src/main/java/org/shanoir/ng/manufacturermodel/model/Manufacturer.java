package org.shanoir.ng.manufacturermodel.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PostLoad;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Manufacturer.
 * 
 * @author msimon
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class Manufacturer extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -4761707953959168601L;

	@NotBlank
	@Column(unique = true)
	@Unique
	@Length(min = 2, max = 200)
	private String name;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "manufacturer/" + getId());
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
