package org.shanoir.ng.study;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * DTO for study cards.
 * 
 * @author msimon
 *
 */
public class StudyCardDTO extends IdNameDTO {

	private Long acquisitionEquipmentId;

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

}
