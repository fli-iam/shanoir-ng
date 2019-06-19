package org.shanoir.ng.importer.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetacquisition.DatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.MrDatasetAcquisitionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * In the strategy pattern this class represents the context.
 * The context holds the variable to the actual strategy in use, that has
 * been chosen on using the required modality.
 * 
 * @author mkain
 *
 */
@Service
public class DatasetAcquisitionContext implements DatasetAcquisitionStrategy {
	
	@Autowired
	private MrDatasetAcquisitionStrategy mrDatasetAcquisitionStrategy;
	
	// add other strategies for other modalities here
	
	private DatasetAcquisitionStrategy datasetAcquisitionStrategy;

	/**
	 * 1) Call first with the given modality to choose the right strategy.
	 * @param modality
	 */
	public void setDatasetAcquisitionStrategy(String modality) {
		if ("MR".equals(modality)) {
			this.datasetAcquisitionStrategy = mrDatasetAcquisitionStrategy;
		}
		// else... add other modalities here
	}

	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob) {
		if (datasetAcquisitionStrategy != null) {
			return datasetAcquisitionStrategy.generateDatasetAcquisitionForSerie(serie, rank, importJob);
		}
		return null;
	}
	
}
