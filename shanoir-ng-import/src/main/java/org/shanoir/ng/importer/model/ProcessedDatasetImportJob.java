package org.shanoir.ng.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Communication object to carry EEG data to be loaded in Shanoir.
 * @author JcomeD
 *
 */
public class ProcessedDatasetImportJob {

	/** Folder where source data is stored. */
	@JsonProperty("workFolder")
	private String workFolder;

	/** Corresponding study. */
	@JsonProperty("studyId")
	private Long studyId;

	/** Corresponding subject. */
	@JsonProperty("subjectId")
	private Long subjectId;

	@JsonProperty("dataset")
	Dataset dataset;

	@JsonProperty("subjectName")
	private String subjectName;

	@JsonProperty("studyName")
	private String studyName;

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
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
