package org.shanoir.ng.importer.examination;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * InstrumentType.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "instrument_type")
@JsonPropertyOrder({ "_links", "id", "name"})
public class InstrumentType extends HalEntity {

	
	private String name;

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



}
