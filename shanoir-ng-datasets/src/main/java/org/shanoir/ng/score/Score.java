package org.shanoir.ng.score;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;

import org.shanoir.ng.examination.VariableAssessment;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Score.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "variableAssessment" })
public class Score extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5995692961509079060L;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variable_assessment_id", nullable = false, updatable = true)
	private VariableAssessment variableAssessment;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "score/" + getId());
	}

	public VariableAssessment getVariableAssessment() {
		return variableAssessment;
	}

	public void setVariableAssessment(VariableAssessment variableAssessment) {
		this.variableAssessment = variableAssessment;
	}

}
