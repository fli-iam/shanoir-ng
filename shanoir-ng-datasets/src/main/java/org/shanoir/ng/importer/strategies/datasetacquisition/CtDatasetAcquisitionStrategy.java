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

package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtProtocol;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.shanoir.ng.importer.strategies.protocol.CtProtocolStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * CT Dataset Acquisition Strategy used to create new CT Dataset Acquisition.
 * Called by the ImportService. Requires an importJob
 * 
 * Refer to Interface for more information
 * 
 * @author atouboul
 *
 */
@Component
public class CtDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(CtDatasetAcquisitionStrategy.class);
	
	@Autowired
	private CtProtocolStrategy protocolStrategy;
	
	@Autowired
	private DatasetStrategy<CtDataset> datasetStrategy;
	
	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob, Attributes dicomAttributes) throws Exception {
		CtDatasetAcquisition datasetAcquisition = new CtDatasetAcquisition();
		LOG.info("Generating DatasetAcquisition for   : {} - {} - Rank:{}",serie.getSequenceName(), serie.getProtocolName(), rank);
		
		datasetAcquisition.setCreationDate(LocalDate.now());
		datasetAcquisition.setRank(rank);
		importJob.getProperties().put(ImportJob.RANK_PROPERTY, String.valueOf(rank));
		datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		datasetAcquisition.setSoftwareRelease(dicomAttributes.getString(Tag.SoftwareVersions));
	
		CtProtocol protocol = protocolStrategy.generateProtocolForSerie(dicomAttributes, serie);
		datasetAcquisition.setCtProtocol(protocol);
	
		// TODO ATO add Compatibility check between study card Equipment and dicomEquipment if not done at front level.
		DatasetsWrapper<CtDataset> datasetsWrapper = datasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, importJob);
		List<Dataset> genericizedList = new ArrayList<>();
		for (Dataset dataset : datasetsWrapper.getDatasets()) {
			dataset.setDatasetAcquisition(datasetAcquisition);
			genericizedList.add(dataset);
		}
		datasetAcquisition.setDatasets(genericizedList);
		return datasetAcquisition;
	}
}
