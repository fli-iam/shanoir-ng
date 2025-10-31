package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.shanoir.ng.shared.dateTime.DateTimeUtils;
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
	public DatasetAcquisition generateDeepDatasetAcquisitionForSerie(String userName, Long subjectId, Serie serie, int rank, AcquisitionAttributes<String> dicomAttributes) throws Exception {
		GenericDatasetAcquisition datasetAcquisition = (GenericDatasetAcquisition) generateFlatDatasetAcquisitionForSerie(
				userName, serie, rank, dicomAttributes.getFirstDatasetAttributes());
		DatasetsWrapper<GenericDataset> datasetsWrapper = datasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, subjectId);
		List<Dataset> genericizedList = new ArrayList<>();
		for (Dataset dataset : datasetsWrapper.getDatasets()) {
			dataset.setDatasetAcquisition(datasetAcquisition);
			genericizedList.add(dataset);
		}
		datasetAcquisition.setDatasets(genericizedList);		
		return datasetAcquisition;
	}

	@Override
	public DatasetAcquisition generateFlatDatasetAcquisitionForSerie(String userName, Serie serie, int rank,
			Attributes attributes) throws Exception {
		LOG.info("Generating GenericDatasetAcquisition for: {} - {} - Rank: {}",
				serie.getSequenceName(), serie.getProtocolName(), rank);
		GenericDatasetAcquisition datasetAcquisition = new GenericDatasetAcquisition();
		datasetAcquisition.setUsername(userName);
		datasetAcquisition.setImportDate(LocalDate.now());
		datasetAcquisition.setSeriesInstanceUID(serie.getSeriesInstanceUID());
		datasetAcquisition.setRank(rank);
		datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		datasetAcquisition.setSoftwareRelease(attributes.getString(Tag.SoftwareVersions));
		datasetAcquisition.setAcquisitionStartTime(
				LocalDateTime.of(DateTimeUtils.pacsStringToLocalDate(attributes.getString(Tag.AcquisitionDate)), 
				DateTimeUtils.stringToLocalTime(attributes.getString(Tag.AcquisitionTime))));
		return datasetAcquisition;
	}

	@Override
	public Dataset generateFlatDataset(Serie serie, org.shanoir.ng.importer.dto.Dataset dataset, int datasetIndex, Long subjectId, Attributes attributes) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'generateFlatDataset'");
	}

}
