/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetacquisition.CtDatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.DatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.GenericDatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.MrDatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.PetDatasetAcquisitionStrategy;
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
	
	@Autowired
	private CtDatasetAcquisitionStrategy ctDatasetAcquisitionStrategy;
	
	@Autowired
	private PetDatasetAcquisitionStrategy petDatasetAcquisitionStrategy;
	
	@Autowired
	private GenericDatasetAcquisitionStrategy genericDatasetAcquisitionStrategy;
	
	// add other strategies for other modalities here

	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob) throws Exception {
		DatasetAcquisitionStrategy datasetAcquisitionStrategy;
		String modality = serie.getModality();
		if ("MR".equals(modality)) {
			datasetAcquisitionStrategy = mrDatasetAcquisitionStrategy;
		} else if ("CT".equals(modality)) {
			datasetAcquisitionStrategy = ctDatasetAcquisitionStrategy;
		} else if ("PT".equals(modality)) {
			datasetAcquisitionStrategy = petDatasetAcquisitionStrategy;
		} else {
			// By default we just create a generic dataset acquisition
			datasetAcquisitionStrategy = genericDatasetAcquisitionStrategy;
		}		
		return datasetAcquisitionStrategy.generateDatasetAcquisitionForSerie(serie, rank, importJob);
	}
	
}
