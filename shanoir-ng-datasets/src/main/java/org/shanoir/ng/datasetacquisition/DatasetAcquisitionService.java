package org.shanoir.ng.datasetacquisition;

import org.shanoir.ng.importer.dto.Serie;

public interface DatasetAcquisitionService<T extends DatasetAcquisition> {
	
	void createDatasetAcquisition(Serie serie);
	
}
