package org.shanoir.ng.dataset.modality;

import java.util.List;
import org.dcm4che3.data.Attributes;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;

/**
 * 
 * This Interface is used by all strategies available for creating a new Dataset
 *  based on a modality obtained obtained in the serie object sent by the MS IMPORT MicroService (cf ImporterService.class)
 *  This interface is usually called from a DatasetAcquisitionStrategy
 *  
 * example of strategy :
 * 	- MrDatasetStrategy -> Modality MR - Magnetic Resonance
 *  - CtDatasetStrategy -> Modality CT
 *  - MrsDatasetStrategy -> Modality MRS - Magnetic Resonance Spectroscopy
 * 
 * @author atouboul
 *
 */

public interface DatasetStrategy {

	DatasetWrapper generateDatasetsForSerie(Attributes dicomAttributes, Serie serie,
			ImportJob importJob);

	MrDataset generateSingleDataset(Attributes dicomAttributes, Serie serie, Dataset dataset, int datasetIndex,
			ImportJob importJob);

	String computeDatasetName(String name, int index);

}