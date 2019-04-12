package org.shanoir.ng.studycard.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Study card.
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

	/** The acquisition equipment. */
	private Long acquisitionEquipmentId;

	/** The center of the study card. */
	private Long centerId;

	/** A studycard might be disabled */
	private boolean disabled;

	/** The name of the study card. */
	@NotBlank
	@Column(unique = true)
	@Unique
	private String name;

	/** The nifti converter of the study card. */
	private Long niftiConverterId;

	/** The study for which is defined the study card. */
	private Long studyId;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "studycard/" + getId());
	}

	/**
	 * @return the acquisitionEquipmentId
	 */
	public Long getAcquisitionEquipmentId() {
		return acquisitionEquipmentId;
	}

	/**
	 * @param acquisitionEquipmentId
	 *            the acquisitionEquipmentId to set
	 */
	public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
		this.acquisitionEquipmentId = acquisitionEquipmentId;
	}

	/**
	 * @return the centerId
	 */
	public Long getCenterId() {
		return centerId;
	}

	/**
	 * @param centerId
	 *            the centerId to set
	 */
	public void setCenterId(Long centerId) {
		this.centerId = centerId;
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
	 * @return the niftiConverterId
	 */
	public Long getNiftiConverterId() {
		return niftiConverterId;
	}

	/**
	 * @param niftiConverterId
	 *            the niftiConverterId to set
	 */
	public void setNiftiConverterId(Long niftiConverterId) {
		this.niftiConverterId = niftiConverterId;
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
