package org.shanoir.ng.examination;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "score")
@JsonPropertyOrder({ "_links", "id", "isSelected", "variableAssessment"})
public class Score extends HalEntity {

	
	@Transient
	private boolean isSelected = false;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "variableAssessment", nullable = false, updatable = true)
	private VariableAssessment variableAssessment;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public VariableAssessment getVariableAssessment() {
		return variableAssessment;
	}

	public void setVariableAssessment(VariableAssessment variableAssessment) {
		this.variableAssessment = variableAssessment;
	}





}
