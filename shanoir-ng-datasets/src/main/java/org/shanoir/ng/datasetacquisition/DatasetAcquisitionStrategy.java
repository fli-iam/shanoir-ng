package org.shanoir.ng.datasetacquisition;

import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.importer.dto.Serie;

/**
 * This interface allow the application to apply differents strategy on the datasets based on 
 * the modality : eg : ct, mr, mrs....
 * 
 * @author atouboul
 *
 */


public interface DatasetAcquisitionStrategy {
	
	// Create a new dataset acquistion 
	DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination);
	
}
