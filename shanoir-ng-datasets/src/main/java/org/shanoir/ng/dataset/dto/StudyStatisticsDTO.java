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

package org.shanoir.ng.dataset.dto;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

import java.sql.Date;


/**
 * DTO for studyStatistics.
 * 
 * @author lvallet
 *
 */
public class StudyStatisticsDTO {

	private Long studyId;

	private String centerName;

	private Long subjectId;

	private String commonName;

	private Long examinationId;

	@LocalDateAnnotations
	private Date examinationDate;

	private Long datasetAcquisitionId;

	@LocalDateAnnotations
	private Date importDate;

	private Long datasetId;

	private String modality;

	private String quality;

	public StudyStatisticsDTO(Long studyId, String centerName, Long subjectId, String commonName,
			Long examinationId, Date examinationDate, Long datasetAcquisitionId, Date importDate,
			Long datasetId, String modality, String quality) {
		this.studyId = studyId;
		this.centerName = centerName;
		this.subjectId = subjectId;
		this.commonName = commonName;
		this.examinationId = examinationId;
		this.examinationDate = examinationDate;
		this.datasetAcquisitionId = datasetAcquisitionId;
		this.importDate = importDate;
		this.datasetId = datasetId;
		this.modality = modality;
		this.quality = quality;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public String getCenterName() {
		return centerName;
	}

	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

	public Date getExaminationDate() {
		return examinationDate;
	}

	public void setExaminationDate(Date examinationDate) {
		this.examinationDate = examinationDate;
	}

	public Long getDatasetAcquisitionId() {
		return datasetAcquisitionId;
	}

	public void setDatasetAcquisitionId(Long datasetAcquisitionId) {
		this.datasetAcquisitionId = datasetAcquisitionId;
	}

	public Date getImportDate() {
		return importDate;
	}

	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}

	public Long getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(Long datasetId) {
		this.datasetId = datasetId;
	}

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}
	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}
}
