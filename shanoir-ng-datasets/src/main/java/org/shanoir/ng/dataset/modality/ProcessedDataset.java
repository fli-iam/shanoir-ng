package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

@Entity
public class ProcessedDataset extends Dataset {

	private static final long serialVersionUID = -5639736911425359454L;

	public static final String datasetType = "Processed";

	private String processedType;

	@Override
	public String getType() {
		return datasetType;
	}

	public String getProcessedType() {
		return processedType;
	}

	public void setProcessedType(String processedType) {
		this.processedType = processedType;
	}

}
