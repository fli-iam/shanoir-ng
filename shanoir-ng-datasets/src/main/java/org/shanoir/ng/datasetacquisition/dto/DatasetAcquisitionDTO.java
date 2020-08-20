/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.datasetacquisition.dto;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.studycard.model.StudyCard;

public class DatasetAcquisitionDTO {
	
	private Long id;

	private Long acquisitionEquipmentId;

	private ExaminationDTO examination;
	
	private StudyCard studyCard;
	
	private Long studyCardTimestamp;

	private Integer rank;

	private String softwareRelease;

	private Integer sortingIndex;
	
	private String type;

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAcquisitionEquipmentId() {
		return acquisitionEquipmentId;
	}

	public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
		this.acquisitionEquipmentId = acquisitionEquipmentId;
	}

	public ExaminationDTO getExamination() {
		return examination;
	}

	public void setExamination(ExaminationDTO examination) {
		this.examination = examination;
	}

	public StudyCard getStudyCard() {
		return studyCard;
	}

	public void setStudyCard(StudyCard studyCard) {
		this.studyCard = studyCard;
	}

	public Long getStudyCardTimestamp() {
		return studyCardTimestamp;
	}

	public void setStudyCardTimestamp(Long studyCardTimestamp) {
		this.studyCardTimestamp = studyCardTimestamp;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getSoftwareRelease() {
		return softwareRelease;
	}

	public void setSoftwareRelease(String softwareRelease) {
		this.softwareRelease = softwareRelease;
	}

	public Integer getSortingIndex() {
		return sortingIndex;
	}

	public void setSortingIndex(Integer sortingIndex) {
		this.sortingIndex = sortingIndex;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
