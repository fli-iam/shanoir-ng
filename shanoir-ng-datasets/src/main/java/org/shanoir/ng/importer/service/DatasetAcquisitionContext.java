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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetacquisition.CtDatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.DatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.GenericDatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.MrDatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.PetDatasetAcquisitionStrategy;
import org.shanoir.ng.importer.strategies.datasetacquisition.XaDatasetAcquisitionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * In the strategy pattern this class represents the context.
 * The context holds the variable to the actual strategy in use,
 * that has been chosen on using the required modality.
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
	private XaDatasetAcquisitionStrategy xaDatasetAcquisitionStrategy;
	
	@Autowired
	private GenericDatasetAcquisitionStrategy genericDatasetAcquisitionStrategy;
	
	@Override
	public DatasetAcquisition generateDeepDatasetAcquisitionForSerie(String userName, Long subjectId, Serie serie, int rank, AcquisitionAttributes<String> dicomAttributes) throws Exception {
		DatasetAcquisitionStrategy datasetAcquisitionStrategy = selectModalityStrategy(serie, dicomAttributes.getFirstDatasetAttributes());
		return datasetAcquisitionStrategy.generateDeepDatasetAcquisitionForSerie(userName, subjectId, serie, rank, dicomAttributes);
	}

	@Override
	public DatasetAcquisition generateFlatDatasetAcquisitionForSerie(String userName, Serie serie, int rank,
			Attributes attributes) throws Exception {
		DatasetAcquisitionStrategy datasetAcquisitionStrategy = selectModalityStrategy(serie, attributes);
		return datasetAcquisitionStrategy.generateFlatDatasetAcquisitionForSerie(userName, serie, rank, attributes);
	}

	@Override
	public Dataset generateFlatDataset(DatasetAcquisition datasetAcquisition, Attributes attributes, Serie serie,
			int datasetIndex, Long subjectId) {
		DatasetAcquisitionStrategy datasetAcquisitionStrategy = selectModalityStrategy(serie, attributes);
		return null;
	}

	// add other strategies for other modalities here
	private DatasetAcquisitionStrategy selectModalityStrategy(Serie serie, Attributes attributes) {
		DatasetAcquisitionStrategy datasetAcquisitionStrategy;
		String modality = serie.getModality();
		if ("MR".equals(modality)) {
			datasetAcquisitionStrategy = mrDatasetAcquisitionStrategy;
		} else if ("CT".equals(modality)) {
			datasetAcquisitionStrategy = ctDatasetAcquisitionStrategy;
		} else if ("PT".equals(modality)) {
			datasetAcquisitionStrategy = petDatasetAcquisitionStrategy;
		} else if ("XA".equals(modality)) {
			datasetAcquisitionStrategy = xaDatasetAcquisitionStrategy;
		}else {
			// By default we just create a generic dataset acquisition
			datasetAcquisitionStrategy = genericDatasetAcquisitionStrategy;
		}
		// Use always SeriesInstanceUID from DICOM files
		String seriesInstanceUID = attributes.getString(Tag.SeriesInstanceUID);
		serie.setSeriesInstanceUID(seriesInstanceUID);
		return datasetAcquisitionStrategy;
	}
	
}
