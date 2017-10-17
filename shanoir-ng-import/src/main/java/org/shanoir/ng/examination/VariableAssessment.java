package org.shanoir.ng.examination;

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
 * VariableAssessment.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "variable_assessment")
@JsonPropertyOrder({ "_links", "id", "instrumentBasedAssessment", "instrumentVariable", "scoreList"})
public class VariableAssessment extends HalEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrumentBasedAssessment", nullable = false, updatable = true)
	private InstrumentBasedAssessment instrumentBasedAssessment;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "instrumentVariable", nullable = false, updatable = true)	
	private InstrumentVariable instrumentVariable;
	

	@OneToMany(mappedBy = "variableAssessment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private List<Score> scoreList ;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}

	public InstrumentBasedAssessment getInstrumentBasedAssessment() {
		return instrumentBasedAssessment;
	}

	public void setInstrumentBasedAssessment(InstrumentBasedAssessment instrumentBasedAssessment) {
		this.instrumentBasedAssessment = instrumentBasedAssessment;
	}

	public InstrumentVariable getInstrumentVariable() {
		return instrumentVariable;
	}

	public void setInstrumentVariable(InstrumentVariable instrumentVariable) {
		this.instrumentVariable = instrumentVariable;
	}

	public List<Score> getScoreList() {
		return scoreList;
	}

	public void setScoreList(List<Score> scoreList) {
		this.scoreList = scoreList;
	}


	


}
