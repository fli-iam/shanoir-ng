package org.shanoir.ng.datasetacquisition.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonTypeName;

@Entity
@JsonTypeName("Processed")
public class ProcessedDatasetAcquisition extends DatasetAcquisition {

	private static final long serialVersionUID = 2424755476816358685L;

    @ManyToMany
    @JoinTable( name = "parent_dataset_acquisition", joinColumns = @JoinColumn( name = "parent_dataset_acquisition_id" ))
	private List<DatasetAcquisition> parentAcquisitions;

	@Override
	public String getType() {
		return "Processed";
	}

	public List<DatasetAcquisition> getParentAcquisitions() {
		return parentAcquisitions;
	}

	public void setParentAcquisitions(List<DatasetAcquisition> parentAcquisitions) {
		this.parentAcquisitions = parentAcquisitions;
	}
}
