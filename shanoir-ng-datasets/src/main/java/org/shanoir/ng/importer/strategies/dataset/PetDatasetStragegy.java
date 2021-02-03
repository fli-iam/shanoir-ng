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
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ProcessedDatasetType;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.datasetexpression.DatasetExpressionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyao
 *
 */
@Component
public class PetDatasetStragegy implements DatasetStrategy<PetDataset>{
	
	@Autowired
	DatasetExpressionContext datasetExpressionContext;

	@Override
	public DatasetsWrapper<PetDataset> generateDatasetsForSerie(Attributes dicomAttributes, Serie serie,
			ImportJob importJob) throws Exception {
		
		DatasetsWrapper<PetDataset> datasetWrapper = new DatasetsWrapper<>();
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

		for (Dataset dataset : serie.getDatasets()) {
			importJob.getProperties().put(ImportJob.INDEX_PROPERTY, String.valueOf(datasetIndex));
			PetDataset petDataset = generateSingleDataset(dicomAttributes, serie, dataset, datasetIndex, importJob);
			datasetWrapper.getDatasets().add(petDataset);
			datasetIndex++;
		}

		return datasetWrapper;
	}

	@Override
	public PetDataset generateSingleDataset(Attributes dicomAttributes, Serie serie, Dataset dataset, int datasetIndex,
			ImportJob importJob) throws Exception {
		PetDataset petDataset = new PetDataset();
		petDataset.setCreationDate(serie.getSeriesDate());
		final String serieDescription = serie.getSeriesDescription();

		DatasetMetadata datasetMetadata = new DatasetMetadata();
		petDataset.setOriginMetadata(datasetMetadata);
		// set the series description as the dataset comment & name
		if (serieDescription != null && !"".equals(serieDescription)) {
			petDataset.getOriginMetadata().setName(computeDatasetName(serieDescription, datasetIndex));
			petDataset.getOriginMetadata().setComment(serieDescription);
		}

		// Pre-select the type Reconstructed dataset
		petDataset.getOriginMetadata().setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);

		// Set the study and the subject
		petDataset.setSubjectId(importJob.getPatients().get(0).getSubject().getId());

		// Set the modality from dicom fields
		petDataset.getOriginMetadata().setDatasetModalityType(DatasetModalityType.PET_DATASET);

		CardinalityOfRelatedSubjects refCardinalityOfRelatedSubjects = null;
		if (petDataset.getSubjectId() != null) {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET;
		} else {
			refCardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.MULTIPLE_SUBJECTS_DATASET;
		}
		petDataset.getOriginMetadata().setCardinalityOfRelatedSubjects(refCardinalityOfRelatedSubjects);
		
		/**
		 *  The part below will generate automatically the datasetExpression according to :
		 *   -  type found in the importJob.serie.datasets.dataset.expressionFormat.type
		 * 
		 *  The DatasetExpressionFactory will return the proper object according to the expression format type and add it to the current petDataset
		 * 
		 **/
		for (ExpressionFormat expressionFormat : dataset.getExpressionFormats()) {
			datasetExpressionContext.setDatasetExpressionStrategy(expressionFormat.getType());
			DatasetExpression datasetExpression = datasetExpressionContext.generateDatasetExpression(serie, importJob, expressionFormat);
			datasetExpression.setDataset(petDataset);
			petDataset.getDatasetExpressions().add(datasetExpression);
		}
		
		DatasetMetadata originalDM = petDataset.getOriginMetadata();
		petDataset.setUpdatedMetadata(originalDM);
		
		return petDataset;
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
