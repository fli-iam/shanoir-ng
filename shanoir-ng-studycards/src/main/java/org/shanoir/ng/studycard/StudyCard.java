package org.shanoir.ng.studycard;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Template.
 *
 * @author msimon
 *
 */
@Entity
@Table(name = "study_cards")
@JsonPropertyOrder({ "_links", "id", "name", "isDisabled" })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyCard extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 1751168445500120935L;

	private boolean disabled;

	private String name;

	@NotNull
	private Long studyId;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "studyCard/" + getId());
	}

	@Override
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "IdOrGenerate")
	@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
	public Long getId() {
		return super.getId();
	}

	/**
	 * @return the disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * @param disabled
	 *            the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId
	 *            the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

}
