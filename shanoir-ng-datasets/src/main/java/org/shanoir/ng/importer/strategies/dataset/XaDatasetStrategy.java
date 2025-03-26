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

package org.shanoir.ng.importer.strategies.dataset;

import org.dcm4che3.data.Attributes;
import org.shanoir.ng.dataset.modality.XaDataset;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetexpression.DatasetExpressionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class XaDatasetStrategy implements DatasetStrategy<XaDataset> {

	@Autowired
	DicomProcessing dicomProcessing;
	
	@Autowired
	DatasetExpressionContext datasetExpressionContext;
	
	@Override
	public DatasetsWrapper<XaDataset> generateDatasetsForSerie(AcquisitionAttributes<String> dicomAttributes, Serie serie,
			ImportJob importJob) throws Exception {
		
		DatasetsWrapper<XaDataset> datasetWrapper = new DatasetsWrapper<>();
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
			XaDataset dataset = generateSingleDataset(dicomAttributes.getDatasetAttributes(anyDataset.getFirstImageSOPInstanceUID()), serie, anyDataset, datasetIndex, importJob);
			datasetWrapper.getDatasets().add(dataset);
			datasetIndex++;
		}

		return datasetWrapper;

	}

	@Override
	public XaDataset generateSingleDataset(Attributes dicomAttributes, Serie serie, Dataset dataset, int datasetIndex,
			ImportJob importJob) throws Exception {
		XaDataset xaDataset = new XaDataset();
		xaDataset.setSOPInstanceUID(dataset.getFirstImageSOPInstanceUID());
		xaDataset.setCreationDate(serie.getSeriesDate());
		final String serieDescription = serie.getSeriesDescription();

		DatasetMetadata datasetMetadata = new DatasetMetadata();
		xaDataset.setOriginMetadata(datasetMetadata);
		// set the series description as the dataset comment & name
		if (serieDescription != null && !"".equals(serieDescription)) {
			xaDataset.getOriginMetadata().setName(computeDatasetName(serieDescription, datasetIndex));
			xaDataset.getOriginMetadata().setComment(serieDescription);
		}

		// Pre-select the type Reconstructed dataset
		xaDataset.getOriginMetadata().setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);

		// Set the study and the subject
		xaDataset.setSubjectId(importJob.getPatients().get(0).getSubject().getId());

		xaDataset.getOriginMetadata().setDatasetModalityType(DatasetModalityType.XA_DATASET);

		CardinalityOfRelatedSubjects refCardinalityOfRelatedSubjects = null;
		if (xaDataset.getSubjectId() != null) {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET;
		} else {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.MULTIPLE_SUBJECTS_DATASET;
		}
		xaDataset.getOriginMetadata().setCardinalityOfRelatedSubjects(refCardinalityOfRelatedSubjects);

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
			datasetExpression.setDataset(xaDataset);
			xaDataset.getDatasetExpressions().add(datasetExpression);
		}
		
		DatasetMetadata originalDM = xaDataset.getOriginMetadata();
		xaDataset.setUpdatedMetadata(originalDM);
		
		return xaDataset;
	}


	/* (non-Javadoc)
	 * @see org.shanoir.ng.dataset.modality.DatasetStrategy#computeDatasetName(java.lang.String, int)
	 */
	@Override
	public String computeDatasetName(String name, int index) {
		if (index == -1) {
			return name;
		} else {
			return name + " " + index;
		}
	}

}
