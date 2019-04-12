package org.shanoir.ng.examination.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * Instrument variable.
 * 
 * @author ifakhfakh
 *
 */
@Entity
public class InstrumentVariable extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 2742726263888793970L;

	/** Is age-dependent variable. */
	private boolean ageDependent;

	/** Is cultural skill-dependent variable. */
	private boolean culturalSkillDependent;

	/** Instrument. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument_id", nullable = false, updatable = true)
	private Instrument instrument;

	/** The domain explored by the variable. */
	@NotNull
	private Integer domain;

	/** Is main variable. */
	private boolean main;

	/** The name. */
	@NotNull
	private String name;

	/** The quality measured by the variable. */
	@NotNull
	private Integer quality;

	/** Is standardized variable. */
	private boolean standardized;

	/** Is sex-dependent variable. */
	private boolean sexDependent;

	/** The Variable Assessment list. */
	@OneToMany(mappedBy = "instrumentVariable", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<VariableAssessment> variableAssessments;

	/**
	 * @return the ageDependent
	 */
	public boolean isAgeDependent() {
		return ageDependent;
	}

	/**
	 * @param ageDependent
	 *            the ageDependent to set
	 */
	public void setAgeDependent(boolean ageDependent) {
		this.ageDependent = ageDependent;
	}

	/**
	 * @return the culturalSkillDependent
	 */
	public boolean isCulturalSkillDependent() {
		return culturalSkillDependent;
	}

	/**
	 * @param culturalSkillDependent
	 *            the culturalSkillDependent to set
	 */
	public void setCulturalSkillDependent(boolean culturalSkillDependent) {
		this.culturalSkillDependent = culturalSkillDependent;
	}

	/**
	 * @return the instrument
	 */
	public Instrument getInstrument() {
		return instrument;
	}

	/**
	 * @param instrument
	 *            the instrument to set
	 */
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	/**
	 * @return the domain
	 */
	public Domain getDomain() {
		return Domain.getDomain(domain);
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(Domain domain) {
		if (domain != null) {
			this.domain = domain.getId();
		}
	}

	/**
	 * @return the main
	 */
	public boolean isMain() {
		return main;
	}

	/**
	 * @param main
	 *            the main to set
	 */
	public void setMain(boolean main) {
		this.main = main;
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
	 * @return the quality
	 */
	public Quality getQuality() {
		return Quality.getQuality(quality);
	}

	/**
	 * @param quality
	 *            the quality to set
	 */
	public void setQuality(Quality quality) {
		if (quality != null) {
			this.quality = quality.getId();
		}
	}

	/**
	 * @return the standardized
	 */
	public boolean isStandardized() {
		return standardized;
	}

	/**
	 * @param standardized
	 *            the standardized to set
	 */
	public void setStandardized(boolean standardized) {
		this.standardized = standardized;
	}

	/**
	 * @return the sexDependent
	 */
	public boolean isSexDependent() {
		return sexDependent;
	}

	/**
	 * @param sexDependent
	 *            the sexDependent to set
	 */
	public void setSexDependent(boolean sexDependent) {
		this.sexDependent = sexDependent;
	}

	/**
	 * @return the variableAssessments
	 */
	public List<VariableAssessment> getVariableAssessmentList() {
		return variableAssessments;
	}

	/**
	 * @param variableAssessments
	 *            the variableAssessments to set
	 */
	public void setVariableAssessmentList(List<VariableAssessment> variableAssessments) {
		this.variableAssessments = variableAssessments;
	}

}
