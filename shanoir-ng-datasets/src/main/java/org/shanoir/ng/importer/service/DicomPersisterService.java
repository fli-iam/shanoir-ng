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

package org.shanoir.ng.importer.service;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetFile;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.service.DicomServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DicomPersisterService {


	@Autowired
	@Qualifier("stowrs")
	DicomServiceApi dicomServiceApi;
	
	public void persistAllForSerie(Serie serie) {
		
		if (serie != null) {
			List<String> dcmFilesToSendToPacs = new ArrayList<String>();
			for (Dataset dataset : serie.getDatasets()) {
				for (ExpressionFormat expressionFormat : dataset.getExpressionFormats()) {
					if (expressionFormat.getType().equals("dcm")) {
						for (DatasetFile datasetFile : expressionFormat.getDatasetFiles()) {
							if (datasetFile.getPath() != null && !datasetFile.getPath().isEmpty()) {
								dcmFilesToSendToPacs.add(datasetFile.getPath());								
							}
						}
					}
				}
			}
			dicomServiceApi.storeDcmFiles(dcmFilesToSendToPacs);
		}
	}
}
