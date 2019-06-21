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

package org.shanoir.ng.importer.strategies.datasetexpression;

import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;

public class NonImageDatasetExpressionStrategy implements DatasetExpressionStrategy{

	@Override
	public DatasetExpression generateDatasetExpression(Serie serie,	ImportJob importJob, ExpressionFormat expressionFormat) {

		// TODO ATO : Non implemented yet. waiting for dicomzip containing  both MR + MRS in a serie.
//		// NON-IMAGES DATASETS & Spectroscopy
//		if(serie!=null && (!serie.getNonImagesPathList().isEmpty())) {
//			final List<DatasetFile> nonImageDatasetFileList = new ArrayList<DatasetFile>();
//			for (final File nonImageFile : dicomFiles) {
//				log.debug("createMrDataset : adding the file " + nonImageFile.getName());
//				final DatasetFile nonImageDatasetFile = new DatasetFile();
//				nonImageDatasetFile.setPath(nonImageFile.toURI().toString().replaceAll(" ", "%20"));
//				nonImageDatasetFileList.add(nonImageDatasetFile);
//			}
//			if (!nonImageDatasetFileList.isEmpty()) {
//				// Create a MR dataset with some fields set by
//				// algorithms
	//
//				final DatasetExpression datasetExpressionNonImage = createDatasetExpression(mrDataset,
//						datasetExpressionToPacs, null);
//				datasetExpressionNonImage.setRefDatasetExpressionFormat(refDatasetExpressionFormatDicom);
//				datasetExpressionNonImage.setRefDatasetProcessing(refDatasetProcessingFormatConversion);
	//
//				for (final DatasetFile datasetFile : nonImageDatasetFileList) {
//					datasetFile.setDatasetExpression(datasetExpressionNonImage);
//					datasetExpressionNonImage.getDatasetFileList().add(datasetFile);
//				}
//			}
//			log.debug("createMrDataset : nonImageDatasetFileList=" + nonImageDatasetFileList);
//		}
		
		return null;
	}



}
