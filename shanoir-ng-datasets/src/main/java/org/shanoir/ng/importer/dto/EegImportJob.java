package org.shanoir.ng.importer.dto;

import java.util.List;

import org.shanoir.ng.dataset.modality.EegDatasetDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Communication object to carry EEG data to be loaded in Shanoir.
 * @author JcomeD
 *
 */
public class EegImportJob {

	/** Folder where source data is stored. */
	@JsonProperty("workFolder")
	private String workFolder;

	/** Corresponding examination. */
	@JsonProperty("examinationId")
	private Long examinationId;

	/** Corresponding study. */
	@JsonProperty("studyId")
	private Long studyId;

	/** Corresponding subject. */
	@JsonProperty("subjectId")
	private Long subjectId;

	/** Corresponding subject name. */
	@JsonProperty("subjectName")
	private String subjectName;

	/** Corresponding study name. */
	@JsonProperty("studyName")
	private String studyName;

	/** Not mandatyory, acquisition equipement. */
	@JsonProperty("acquisitionEquipmentId")
	private Long acquisitionEquipmentId;

	/** List of associated datasets. */
	@JsonProperty("datasets")
	private List<EegDatasetDTO> datasets;

	public List<EegDatasetDTO> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<EegDatasetDTO> datasets) {
		this.datasets = datasets;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public Long getAcquisitionEquipmentId() {
		return acquisitionEquipmentId;
	}

	public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
		this.acquisitionEquipmentId = acquisitionEquipmentId;
	}

	public String getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
	
}
