package org.shanoir.ng.template;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Template.
 * 
 * @author msimon
 *
 */
@Entity
@Table(name = "template")
@JsonPropertyOrder({ "_links", "id", "data" })
public class Template extends HalEntity {

	private String data;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "template/" + getId());
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}

}
