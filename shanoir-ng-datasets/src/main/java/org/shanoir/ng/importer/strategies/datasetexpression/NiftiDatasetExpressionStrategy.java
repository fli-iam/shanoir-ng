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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.processing.DatasetProcessingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NiftiDatasetExpressionStrategy implements DatasetExpressionStrategy {

	private static final String SUB_PREFIX = "sub-";
	
	private static final String SES_PREFIX = "ses-";
	
	private static final String ANAT = "anat";
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(NiftiDatasetExpressionStrategy.class);
	
	@Value("${datasets-data}")
	private String niftiStorageDir;
	
	@Override
	public DatasetExpression generateDatasetExpression(Serie serie, ImportJob importJob,
			ExpressionFormat expressionFormat) {
		
		DatasetExpression niftiDatasetExpression = new DatasetExpression();
		niftiDatasetExpression.setCreationDate(LocalDateTime.now());
		niftiDatasetExpression.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
		niftiDatasetExpression.setDatasetProcessingType(DatasetProcessingType.FORMAT_CONVERSION);
		
		niftiDatasetExpression.setNiftiConverterId(importJob.getConverterId());
				
		niftiDatasetExpression.setOriginalNiftiConversion(true);
		if (Boolean.TRUE.equals(serie.getIsMultiFrame())) {
			niftiDatasetExpression.setMultiFrame(true);
			niftiDatasetExpression.setFrameCount(serie.getMultiFrameCount());
		}
		
		if (expressionFormat != null && expressionFormat.getType().equals("nii")) {

			final String subLabel = SUB_PREFIX + importJob.getPatients().get(0).getSubject().getName();
			// TODO BIDS: Remove ses level if only one examination, add ses level if new examination imported for the same subject
			final String sesLabel = SES_PREFIX + importJob.getExaminationId();
			// TODO BIDS: Get data type (anat, func, dwi, fmap, meg and beh) from MrDatasetNature and/or ExploredEntity
			final String dataTypeLabel = ANAT + "/";
			
			final File outDir = new File(niftiStorageDir + File.separator + subLabel + File.separator + sesLabel + File.separator + dataTypeLabel + File.separator);
			outDir.mkdirs();
			
			for (org.shanoir.ng.importer.dto.DatasetFile datasetFile : expressionFormat.getDatasetFiles()) {

				File srcFile = new File(datasetFile.getPath().replace("file:" , ""));
				String originalNiftiName = srcFile.getAbsolutePath().substring(datasetFile.getPath().lastIndexOf('/') + 1);
				File destFile = new File(outDir.getAbsolutePath() + File.separator + originalNiftiName);
				Path niftiFinalLocation = null;
				try {
					niftiFinalLocation = Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					LOG.error("IOException generating nifti Dataset Expression", e);
				}

				if (niftiFinalLocation != null) {
					DatasetFile niftiDatasetFile = new DatasetFile();
					niftiDatasetFile.setPacs(false);
					niftiDatasetFile.setPath(niftiFinalLocation.toUri().toString().replaceAll(" ", "%20"));
					niftiDatasetExpression.getDatasetFiles().add(niftiDatasetFile);
					niftiDatasetFile.setDatasetExpression(niftiDatasetExpression);
				}
			}
		}
		return niftiDatasetExpression;
	}
}
