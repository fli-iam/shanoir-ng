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

import org.hibernate.annotations.Cascade;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Instrument.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "instrument")
@JsonPropertyOrder({ "_links", "id", "name", "acronym", "isMonoDomain", "childInstrumentList", "instrumentBasedAssessmentList",
	"instrumentDefinitionArticle", "instrumentVariableList", "parentInstrument", "instrumentType", "passationMode", "instrumentRefDomainList"})
public class Instrument extends HalEntity {
	
	private String name;

	private String acronym;
	
	private boolean isMonoDomain;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parentInstrument", fetch = FetchType.LAZY)
	private List<Instrument> childInstrumentList;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument", fetch = FetchType.LAZY)
	private List<InstrumentBasedAssessment> instrumentBasedAssessmentList;
	
	@ManyToOne
	@JoinColumn(name = "instrumentDefinitionArticle")
	private ScientificArticle instrumentDefinitionArticle;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrument", fetch = FetchType.LAZY)
	private List<InstrumentVariable> instrumentVariableList;
	
	@ManyToOne
	@JoinColumn(name = "parentInstrument")
	private Instrument parentInstrument;
	
	private Long instrumentType;
	
	private Long passationMode;
	

	@OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL)
	@Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private List<InstrumentRefDomain> instrumentRefDomainList;
	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}
	public String getAcronym() {
		return acronym;
	}
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
	public List<Instrument> getChildInstrumentList() {
		return childInstrumentList;
	}
	public void setChildInstrumentList(List<Instrument> childInstrumentList) {
		this.childInstrumentList = childInstrumentList;
	}
	public List<InstrumentBasedAssessment> getInstrumentBasedAssessmentList() {
		return instrumentBasedAssessmentList;
	}
	public void setInstrumentBasedAssessmentList(List<InstrumentBasedAssessment> instrumentBasedAssessmentList) {
		this.instrumentBasedAssessmentList = instrumentBasedAssessmentList;
	}
	public ScientificArticle getInstrumentDefinitionArticle() {
		return instrumentDefinitionArticle;
	}
	public void setInstrumentDefinitionArticle(ScientificArticle instrumentDefinitionArticle) {
		this.instrumentDefinitionArticle = instrumentDefinitionArticle;
	}
	public List<InstrumentVariable> getInstrumentVariableList() {
		return instrumentVariableList;
	}
	public void setInstrumentVariableList(List<InstrumentVariable> instrumentVariableList) {
		this.instrumentVariableList = instrumentVariableList;
	}
	public boolean isMonoDomain() {
		return isMonoDomain;
	}
	public void setMonoDomain(boolean isMonoDomain) {
		this.isMonoDomain = isMonoDomain;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Instrument getParentInstrument() {
		return parentInstrument;
	}
	public void setParentInstrument(Instrument parentInstrument) {
		this.parentInstrument = parentInstrument;
	}

	public List<InstrumentRefDomain> getInstrumentRefDomainList() {
		return instrumentRefDomainList;
	}
	public void setInstrumentRefDomainList(List<InstrumentRefDomain> instrumentRefDomainList) {
		this.instrumentRefDomainList = instrumentRefDomainList;
	}
	public Long getInstrumentType() {
		return instrumentType;
	}
	public void setInstrumentType(Long instrumentType) {
		this.instrumentType = instrumentType;
	}
	public Long getPassationMode() {
		return passationMode;
	}
	public void setPassationMode(Long passationMode) {
		this.passationMode = passationMode;
	}

	
	
}
