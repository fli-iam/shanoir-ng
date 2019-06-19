package org.shanoir.ng.study.dto;

import org.shanoir.ng.shared.core.model.IdName;

/**
 * DTO for study cards.
 * 
 * @author msimon
 *
 */
public class StudyCardDTO extends IdName {

	private Long acquisitionEquipmentId;
	
	private Long centerId;
	
	private Long niftiConverterId;
	
	private Long studyId;

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

	public Long getCenterId() {
		return centerId;
	}

	public void setCenterId(Long centerId) {
		this.centerId = centerId;
	}

	public Long getNiftiConverterId() {
		return niftiConverterId;
	}

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
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

}
