package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(GenericDatasetAcquisitionStrategy.class);
	
	@Autowired
	private DatasetStrategy<GenericDataset> datasetStrategy;
	
	
	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob, Attributes dicomAttributes) throws Exception {
		GenericDatasetAcquisition datasetAcquisition = new GenericDatasetAcquisition();
		LOG.info("Generating DatasetAcquisition for   : {} - {} - Rank:{}",serie.getSequenceName(), serie.getProtocolName(), rank);
		datasetAcquisition.setRank(rank);
		importJob.getProperties().put(ImportJob.RANK_PROPERTY, String.valueOf(rank));
		datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		datasetAcquisition.setSoftwareRelease(dicomAttributes.getString(Tag.SoftwareVersions));
		DatasetsWrapper<GenericDataset> datasetsWrapper = datasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, importJob);
		List<Dataset> genericizedList = new ArrayList<>();
		for (Dataset dataset : datasetsWrapper.getDatasets()) {
			dataset.setDatasetAcquisition(datasetAcquisition);
			genericizedList.add(dataset);
		}
		datasetAcquisition.setDatasets(genericizedList);		
		return datasetAcquisition;
	}
}
