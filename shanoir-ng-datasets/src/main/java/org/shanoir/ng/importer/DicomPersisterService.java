package org.shanoir.ng.importer;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetFile;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.service.DicomServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DicomPersisterService {

	private static final Logger LOG = LoggerFactory.getLogger(DicomPersisterService.class);

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
