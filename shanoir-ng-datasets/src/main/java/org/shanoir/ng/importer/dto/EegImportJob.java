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
	@JsonProperty("frontStudyId")
	private Long frontStudyId;

	/** Corresponding subject. */
	@JsonProperty("subjectId")
	private Long subjectId;

	/** Not mandatyory, acquisition equipement. */
	@JsonProperty("frontAcquisitionEquipmentId")
	private Long frontAcquisitionEquipmentId;

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

	public Long getFrontStudyId() {
		return frontStudyId;
	}

	public void setFrontStudyId(Long frontStudyId) {
		this.frontStudyId = frontStudyId;
	}

	public Long getFrontAcquisitionEquipmentId() {
		return frontAcquisitionEquipmentId;
	}

	public void setFrontAcquisitionEquipmentId(Long frontAcquisitionEquipmentId) {
		this.frontAcquisitionEquipmentId = frontAcquisitionEquipmentId;
	}

	public String getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}
}
