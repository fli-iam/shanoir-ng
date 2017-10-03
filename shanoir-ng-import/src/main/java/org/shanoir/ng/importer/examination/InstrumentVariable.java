package org.shanoir.ng.importer.examination;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * InstrumentVariable.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "instrument_variable")
@JsonPropertyOrder({ "_links", "id", "name","main","standardized","ageDependent","sexDependent","culturalSkillDependent",
	"instrument", "domain", "quality", "variableAssessmentList"})
public class InstrumentVariable extends HalEntity {

	private String name;
	private boolean main;
	private boolean standardized;
	private boolean ageDependent;
	private boolean sexDependent;
	private boolean culturalSkillDependent;

	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrument", nullable = false, updatable = true)
	private Instrument instrument;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "domain", nullable = false, updatable = true)
	private Domain domain;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quality", nullable = false, updatable = true)
	private Quality quality;
	
	
	@OneToMany(mappedBy = "instrumentVariable", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<VariableAssessment> variableAssessmentList;

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

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public boolean isStandardized() {
		return standardized;
	}

	public void setStandardized(boolean standardized) {
		this.standardized = standardized;
	}

	public boolean isAgeDependent() {
		return ageDependent;
	}

	public void setAgeDependent(boolean ageDependent) {
		this.ageDependent = ageDependent;
	}

	public boolean isSexDependent() {
		return sexDependent;
	}

	public void setSexDependent(boolean sexDependent) {
		this.sexDependent = sexDependent;
	}

	public boolean isCulturalSkillDependent() {
		return culturalSkillDependent;
	}

	public void setCulturalSkillDependent(boolean culturalSkillDependent) {
		this.culturalSkillDependent = culturalSkillDependent;
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

	public Quality getQuality() {
		return quality;
	}

	public void setQuality(Quality quality) {
		this.quality = quality;
	}

	public List<VariableAssessment> getVariableAssessmentList() {
		return variableAssessmentList;
	}

	public void setVariableAssessmentList(List<VariableAssessment> variableAssessmentList) {
		this.variableAssessmentList = variableAssessmentList;
	}


	
}
