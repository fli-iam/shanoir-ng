package org.shanoir.ng.importer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Communication object to carry EEG data to be loaded in Shanoir.
 * @author JcomeD
 *
 */
public class EegImportJob extends ImportJob {

	private static final long serialVersionUID = -5482473150099609081L;

	
	@JsonProperty("datasets")
	List<EegDataset> datasets;

	@JsonProperty("subjectId")
	private Long subjectId;

	public List<EegDataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<EegDataset> datasets) {
		this.datasets = datasets;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}
}
