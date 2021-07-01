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
import org.shanoir.ng.dataset.modality.CtDataset;
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
public class CtDatasetStrategy implements DatasetStrategy<CtDataset> {

	@Autowired
	DicomProcessing dicomProcessing;
	
	@Autowired
	DatasetExpressionContext datasetExpressionContext;
	
	@Override
	public DatasetsWrapper<CtDataset> generateDatasetsForSerie(Attributes dicomAttributes, Serie serie,
			ImportJob importJob) throws Exception {
		
		DatasetsWrapper<CtDataset> datasetWrapper = new DatasetsWrapper<>();
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
			CtDataset dataset = generateSingleDataset(dicomAttributes, serie, anyDataset, datasetIndex, importJob);
			datasetWrapper.getDatasets().add(dataset);
			datasetIndex++;
		}

		return datasetWrapper;

	}

	@Override
	public CtDataset generateSingleDataset(Attributes dicomAttributes, Serie serie, Dataset dataset, int datasetIndex,
			ImportJob importJob) throws Exception {
		CtDataset ctDataset = new CtDataset();
		ctDataset.setCreationDate(serie.getSeriesDate());
		final String serieDescription = serie.getSeriesDescription();

		DatasetMetadata datasetMetadata = new DatasetMetadata();
		ctDataset.setOriginMetadata(datasetMetadata);
		// set the series description as the dataset comment & name
		if (serieDescription != null && !"".equals(serieDescription)) {
			ctDataset.getOriginMetadata().setName(computeDatasetName(serieDescription, datasetIndex));
			ctDataset.getOriginMetadata().setComment(serieDescription);
		}

		// Pre-select the type Reconstructed dataset
		ctDataset.getOriginMetadata().setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);

		// Set the study and the subject
		ctDataset.setSubjectId(importJob.getPatients().get(0).getSubject().getId());

		// Set the modality from dicom fields
		// TODO  :VERIFY NOT NEEDED ANY MORE ?
		ctDataset.getOriginMetadata().setDatasetModalityType(DatasetModalityType.CT_DATASET);

		CardinalityOfRelatedSubjects refCardinalityOfRelatedSubjects = null;
		if (ctDataset.getSubjectId() != null) {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET;
		} else {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.MULTIPLE_SUBJECTS_DATASET;
		}
		ctDataset.getOriginMetadata().setCardinalityOfRelatedSubjects(refCardinalityOfRelatedSubjects);
		
		


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
			datasetExpression.setDataset(ctDataset);
			ctDataset.getDatasetExpressions().add(datasetExpression);
		}
		
		DatasetMetadata originalDM = ctDataset.getOriginMetadata();
		ctDataset.setUpdatedMetadata(originalDM);
		
		return ctDataset;
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
