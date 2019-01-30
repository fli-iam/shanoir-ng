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

package org.shanoir.ng.examination;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Instrument.
 * 
 * @author ifakhfakh
 *
 */
@Entity
public class Instrument extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -2928576247765518757L;

	/** The acronym. */
	@NotNull
	private String acronym;

	/** The child instrument list. */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parentInstrument", fetch = FetchType.LAZY)
	private List<Instrument> childInstruments;

	/** List of the instrument based assessment related to this instrument. */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument", fetch = FetchType.LAZY)
	private List<InstrumentBasedAssessment> instrumentBasedAssessments;

	/** Scientific Article. It must be an instrument definition article. */
	@ManyToOne
	@JoinColumn(name = "instrument_definition_article_id")
	private ScientificArticle instrumentDefinitionArticle;

	/** Instrument application domain. */
	@ElementCollection
	@CollectionTable(name = "instrument_domains", joinColumns = @JoinColumn(name = "instrument_id"))
	@Column(name = "domain")
	private List<Integer> domains;

	/** Instrument type. */
	@NotNull
	private Integer instrumentType;

	/** The instrumentVariable list. */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument", fetch = FetchType.LAZY)
	private List<InstrumentVariable> instrumentVariables;

	/** Is Mono domain. */
	private boolean monoDomain;

	/** The name. */
	@NotNull
	private String name;

	/** The parent instrument. */
	@ManyToOne
	@JoinColumn(name = "parent_instrument_id")
	private Instrument parentInstrument;

	/** Instrument passation mode. */
	@NotNull
	private Integer passationMode;

	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}

	/**
	 * @param acronym
	 *            the acronym to set
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	/**
	 * @return the childInstruments
	 */
	public List<Instrument> getChildInstruments() {
		return childInstruments;
	}

	/**
	 * @param childInstruments
	 *            the childInstruments to set
	 */
	public void setChildInstruments(List<Instrument> childInstruments) {
		this.childInstruments = childInstruments;
	}

	/**
	 * @return the instrumentBasedAssessments
	 */
	public List<InstrumentBasedAssessment> getInstrumentBasedAssessments() {
		return instrumentBasedAssessments;
	}

	/**
	 * @param instrumentBasedAssessments
	 *            the instrumentBasedAssessments to set
	 */
	public void setInstrumentBasedAssessments(List<InstrumentBasedAssessment> instrumentBasedAssessments) {
		this.instrumentBasedAssessments = instrumentBasedAssessments;
	}

	/**
	 * @return the instrumentDefinitionArticle
	 */
	public ScientificArticle getInstrumentDefinitionArticle() {
		return instrumentDefinitionArticle;
	}

	/**
	 * @param instrumentDefinitionArticle
	 *            the instrumentDefinitionArticle to set
	 */
	public void setInstrumentDefinitionArticle(ScientificArticle instrumentDefinitionArticle) {
		this.instrumentDefinitionArticle = instrumentDefinitionArticle;
	}

	/**
	 * @return the domains
	 */
	public List<Domain> getDomains() {
		if (domains == null) {
			return null;
		} else {
			final List<Domain> results = new ArrayList<>();
			for (Integer domainId : domains) {
				results.add(Domain.getDomain(domainId));
			}
			return results;
		}
	}

	/**
	 * @param domains
	 *            the domains to set
	 */
	public void setDomains(List<Domain> domains) {
		if (domains == null) {
			this.domains = null;
		} else {
			this.domains = new ArrayList<>();
			for (Domain domain: domains) {
				this.domains.add(domain.getId());
			}
		}
	}

	/**
	 * @return the instrumentType
	 */
	public InstrumentType getInstrumentType() {
		return InstrumentType.getType(instrumentType);
	}

	/**
	 * @param instrumentType
	 *            the instrumentType to set
	 */
	public void setInstrumentType(InstrumentType instrumentType) {
		if (instrumentType != null) {
			this.instrumentType = instrumentType.getId();
		}
	}

	/**
	 * @return the instrumentVariables
	 */
	public List<InstrumentVariable> getInstrumentVariables() {
		return instrumentVariables;
	}

	/**
	 * @param instrumentVariables
	 *            the instrumentVariables to set
	 */
	public void setInstrumentVariables(List<InstrumentVariable> instrumentVariables) {
		this.instrumentVariables = instrumentVariables;
	}

	/**
	 * @return the monoDomain
	 */
	public boolean isMonoDomain() {
		return monoDomain;
	}

	/**
	 * @param monoDomain
	 *            the monoDomain to set
	 */
	public void setMonoDomain(boolean monoDomain) {
		this.monoDomain = monoDomain;
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

	/**
	 * @return the parentInstrument
	 */
	public Instrument getParentInstrument() {
		return parentInstrument;
	}

	/**
	 * @param parentInstrument
	 *            the parentInstrument to set
	 */
	public void setParentInstrument(Instrument parentInstrument) {
		this.parentInstrument = parentInstrument;
	}

	/**
	 * @return the passationMode
	 */
	public PassationMode getPassationMode() {
		return PassationMode.getMode(passationMode);
	}

	/**
	 * @param passationMode
	 *            the passationMode to set
	 */
	public void setPassationMode(PassationMode passationMode) {
		if (passationMode != null) {
			this.passationMode = passationMode.getId();
		}
	}

}
