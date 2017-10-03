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
 * InstrumentRefDomain.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "instrument_ref_domain")
@JsonPropertyOrder({ "_links", "id", "instrument", "domain"})
public class InstrumentRefDomain extends HalEntity {

	
	@ManyToOne
	@JoinColumn(name = "instrument")
	private Instrument instrument;
	
	@ManyToOne
	@JoinColumn(name = "domain")
	private Domain domain;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}


	


}
