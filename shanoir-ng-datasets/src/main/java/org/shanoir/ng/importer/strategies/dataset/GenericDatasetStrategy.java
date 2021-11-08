package org.shanoir.ng.importer.strategies.dataset;

import org.dcm4che3.data.Attributes;

import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ProcessedDatasetType;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetexpression.DatasetExpressionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericDatasetStrategy implements DatasetStrategy<GenericDataset> {
	
	@Autowired
	DicomProcessing dicomProcessing;
	
	@Autowired
	DatasetExpressionContext datasetExpressionContext;
	
	@Override
	public DatasetsWrapper<GenericDataset> generateDatasetsForSerie(Attributes dicomAttributes, Serie serie,
			ImportJob importJob) throws Exception {
		DatasetsWrapper<GenericDataset> datasetWrapper = new DatasetsWrapper<>();
		/**
		 * retrieve number of dataset in current serie if Number of dataset > 1 then
		 * each dataset will be named with an int at the end of the name. else the is
		 * only one dataset => no need for extension.
		 */
		int datasetIndex;
		if (serie.getDatasets().size() > 1) {
			datasetIndex = 1;
		} else {
			datasetIndex = -1;
		}

		for (Dataset anyDataset : serie.getDatasets()) {
			importJob.getProperties().put(ImportJob.INDEX_PROPERTY, String.valueOf(datasetIndex));
			GenericDataset dataset = generateSingleDataset(dicomAttributes, serie, anyDataset, datasetIndex, importJob);
			datasetWrapper.getDatasets().add(dataset);
			datasetIndex++;
		}

		return datasetWrapper;

	}

	@Override
	public GenericDataset generateSingleDataset(Attributes dicomAttributes, Serie serie, Dataset dataset,
			int datasetIndex, ImportJob importJob) throws Exception {
		GenericDataset genericDataset = new GenericDataset();
		genericDataset.setCreationDate(serie.getSeriesDate());
		final String serieDescription = serie.getSeriesDescription();

		DatasetMetadata datasetMetadata = new DatasetMetadata();
		genericDataset.setOriginMetadata(datasetMetadata);
		// set the series description as the dataset comment & name
		if (serieDescription != null && !"".equals(serieDescription)) {
			genericDataset.getOriginMetadata().setName(computeDatasetName(serieDescription, datasetIndex));
			genericDataset.getOriginMetadata().setComment(serieDescription);
		}

		// Pre-select the type Reconstructed dataset
		genericDataset.getOriginMetadata().setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);

		// Set the study and the subject
		genericDataset.setSubjectId(importJob.getPatients().get(0).getSubject().getId());

		// Set the modality from dicom fields
		genericDataset.getOriginMetadata().setDatasetModalityType(DatasetModalityType.GENERIC_DATASET);

		CardinalityOfRelatedSubjects refCardinalityOfRelatedSubjects = null;
		if (genericDataset.getSubjectId() != null) {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET;
		} else {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.MULTIPLE_SUBJECTS_DATASET;
		}
		genericDataset.getOriginMetadata().setCardinalityOfRelatedSubjects(refCardinalityOfRelatedSubjects);
		
		/**
		 *  The part below will generate automatically the datasetExpression according to :
		 *   -  type found in the importJob.serie.datasets.dataset.expressionFormat.type
		 * 
		 *  The DatasetExpressionFactory will return the proper object according to the expression format type and add it to the current ctDataset
		 * 
		 **/
		for (ExpressionFormat expressionFormat : dataset.getExpressionFormats()) {
			datasetExpressionContext.setDatasetExpressionStrategy(expressionFormat.getType());
			DatasetExpression datasetExpression = datasetExpressionContext.generateDatasetExpression(serie, importJob, expressionFormat);
			datasetExpression.setDataset(genericDataset);
			genericDataset.getDatasetExpressions().add(datasetExpression);
		}
		
		DatasetMetadata originalDM = genericDataset.getOriginMetadata();
		genericDataset.setUpdatedMetadata(originalDM);
		
		return genericDataset;
	}

	@Override
	public String computeDatasetName(String name, int index) {
		if (index == -1) {
			return name;
		} else {
			return name + " " + index;
		}
	}

}
