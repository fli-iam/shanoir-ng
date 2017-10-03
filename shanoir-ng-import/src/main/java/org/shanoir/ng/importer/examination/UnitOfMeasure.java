package org.shanoir.ng.importer.examination;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * UnitOfMeasure.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "unit_of_measure")
@JsonPropertyOrder({ "_links", "id", "name","ucum"})
public class UnitOfMeasure extends HalEntity {

	private String name;
	private boolean ucum;
	
	

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public boolean isUcum() {
		return ucum;
	}


	public void setUcum(boolean ucum) {
		this.ucum = ucum;
	}


	

}
