package org.shanoir.ng.importer.strategies.datasetacquisition;

import org.shanoir.ng.datasetacquisition.DatasetAcquisition;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;

/**
 * 
 * This Interface is used by all strategies available for creating a new DatasetAcquisition
 *  based on a modality obtained obtained in the serie object sent by the MS IMPORT MicroService (cf ImporterService.class)
 *  
 * example of strategy :
 * 	- MrDatasetAcquisitionStrategy -> Modality MR - Magnetic Resonance
 *  - CtDatasetAcquisitionStrategy -> Modality CT
 *  - MrsDatasetAcquisitionStrategy -> Modality MRS - Magnetic Resonance Spectroscopy
 * 
 * @author atouboul
 *
 */
public interface DatasetAcquisitionStrategy {
	
	// Create a new dataset acquisition 
	DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob);

}