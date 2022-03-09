package org.shanoir.ng.importer.dto;

import java.util.List;

import org.shanoir.ng.dataset.modality.EegDatasetDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Communication object to carry EEG data to be loaded in Shanoir.
 * @author JcomeD
 *
 */
public class EegImportJob extends ImportJob {

	private static final long serialVersionUID = 2425683448060201704L;

	/** List of associated datasets. */
	@JsonProperty("subjectId")
	private Long subjectId;

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
}
