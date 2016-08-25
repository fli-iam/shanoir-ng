package org.shanoir.studycard.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author msimon
 *
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "org.shanoir.studycard.model.StudyCard.findAll",
            query = "select sc from StudyCard sc")
})
public class StudyCard {

	/** ID. */
	@Id
	@GeneratedValue
	private long id;

	/** The name of the study card. */
	private String name;

	/** A studycard might be disabled */
	private boolean isDisabled;

	/** The study for which is defined the study card. */
	private Long studyId;

	/** The acquisition equipment. */
	private Long acquisitionEquipmentId;

	/** The center of the study card. */
	private Long centerId;

	/** List of the study card elements. */
	//private List<StudyCardElement> studyCardElementList = new ArrayList<StudyCardElement>(0);
	//private List<Long> studyCardElementList;

	/** List of the study card rules. */
	//private List<StudyCardRule> studyCardRuleList = new ArrayList<StudyCardRule>(0);
	//private List<Long> studyCardRuleList;

	/** The nifti converter of the study card. */
	private Long niftiConverter;

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the isDisabled
	 */
	public boolean isDisabled() {
		return isDisabled;
	}

	/**
	 * @param isDisabled the isDisabled to set
	 */
	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the acquisitionEquipmentId
	 */
	public Long getAcquisitionEquipmentId() {
		return acquisitionEquipmentId;
	}

	/**
	 * @param acquisitionEquipmentId the acquisitionEquipmentId to set
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
	 * @param centerId the centerId to set
	 */
	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}

	/**
	 * @return the niftiConverter
	 */
	public Long getNiftiConverter() {
		return niftiConverter;
	}

	/**
	 * @param niftiConverter the niftiConverter to set
	 */
	public void setNiftiConverter(Long niftiConverter) {
		this.niftiConverter = niftiConverter;
	}
	
}
