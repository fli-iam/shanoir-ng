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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.XaDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaProtocol;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.shanoir.ng.importer.strategies.protocol.XaProtocolStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lvallet
 *
 */
@Component
public class XaDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy{


	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(XaDatasetAcquisitionStrategy.class);
	
	@Autowired
	private XaProtocolStrategy protocolStrategy;
	
	@Autowired
	private DatasetStrategy<XaDataset> datasetStrategy;
	
	
	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, String seriesInstanceUID, int rank, ImportJob importJob, AcquisitionAttributes<String> dicomAttributes)
			throws Exception {
		
		XaDatasetAcquisition datasetAcquisition = new XaDatasetAcquisition();
		LOG.info("Generating DatasetAcquisition for   : {} - {} - Rank:{}", serie.getSequenceName(), serie.getProtocolName(), rank);
		datasetAcquisition.setImportDate(LocalDate.now());
		datasetAcquisition.setUsername(importJob.getUsername());
		datasetAcquisition.setSeriesInstanceUID(seriesInstanceUID);
		datasetAcquisition.setRank(rank);
		datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		datasetAcquisition.setSoftwareRelease(dicomAttributes.getFirstDatasetAttributes().getString(Tag.SoftwareVersions));
		LocalDateTime acquisitionStartTime = DicomProcessing.parseAcquisitionStartTime(dicomAttributes.getFirstDatasetAttributes().getString(Tag.AcquisitionDate), 
				dicomAttributes.getFirstDatasetAttributes().getString(Tag.AcquisitionTime));
		datasetAcquisition.setAcquisitionStartTime(acquisitionStartTime);
		XaProtocol protocol = protocolStrategy.generateProtocolForSerie(dicomAttributes, serie);
		datasetAcquisition.setXaProtocol(protocol);
	
		// TODO ATO add Compatibility check between study card Equipment and dicomEquipment if not done at front level.
		DatasetsWrapper<XaDataset> datasetsWrapper = datasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, importJob);
		List<Dataset> genericizedList = new ArrayList<>();
		for (Dataset dataset : datasetsWrapper.getDatasets()) {
			dataset.setDatasetAcquisition(datasetAcquisition);
			genericizedList.add(dataset);
		}
		datasetAcquisition.setDatasets(genericizedList);		
		return datasetAcquisition;
	}
}
