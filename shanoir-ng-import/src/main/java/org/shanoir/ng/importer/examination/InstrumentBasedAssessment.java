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
 * InstrumentBasedAssessment.
 * 
 * @author ifakhfakh
 *
 */
@Entity
@Table(name = "instrument_based_assessment")
@JsonPropertyOrder({ "_links", "id" , "examination", "instrument", "variableAssessmentList"})
public class InstrumentBasedAssessment extends HalEntity {

	@ManyToOne
	@JoinColumn(name = "examination",updatable = true, nullable = false)
	private Examination examination;
	
	@ManyToOne
	@JoinColumn(name = "instrument",updatable = true, nullable = false)
	private Instrument instrument;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "instrumentBasedAssessment", fetch = FetchType.LAZY)
	@Cascade({ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
	private List<VariableAssessment> variableAssessmentList ;

	

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "examination/" + getId());
	}


	
}
