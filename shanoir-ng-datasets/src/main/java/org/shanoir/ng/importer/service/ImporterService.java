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
<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/importer/service/ImporterService.java
=======

package org.shanoir.ng.importer;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/importer/ImporterService.java

package org.shanoir.ng.importer.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ImporterService {
		
	@Autowired
	private ExaminationService examinationService;

	@Autowired
	private DatasetAcquisitionContext datasetAcquisitionContext;
	
	@Autowired
	private DatasetAcquisitionRepository datasetAcquisitionRepository;
	
	@Autowired 
	private DicomPersisterService dicomPersisterService;
	
	private ImportJob importJob;
	
	public void setImportJob(ImportJob importJob) {
		this.importJob = importJob;
	}
	
	// TODO 
	public void retrieveMetadataInDicom() {
	}
	
	public void buildDatasets() {
	}
	
	public void createAllDatasetAcquisition() {
		Examination examination = examinationService.findById(importJob.getExaminationId());
		if (examination != null) {
			int rank = 0;
			for (Patient patient : importJob.getPatients()) {
				for (Study study : patient.getStudies()) {
					for (Serie serie : study.getSeries() ) {
						if (serie.getSelected() != null && serie.getSelected()) {
							createDatasetAcquisitionForSerie(serie, rank, examination);
							rank++;
						}
					}
				}
			}
		}
	}
	
	public void createDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination) {
		if (serie.getModality() != null) {
			// Added Temporary check on serie in order not to generate dataset acquisition for series without images.
			if (serie.getDatasets() != null && !serie.getDatasets().isEmpty()) {
				if (serie.getDatasets().get(0).getExpressionFormats() != null) {
					if (serie.getDatasets().get(0).getExpressionFormats().size() > 0) {
						datasetAcquisitionContext.setDatasetAcquisitionStrategy(serie.getModality());
						DatasetAcquisition datasetAcquisition = datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, rank, importJob);
						datasetAcquisition.setExamination(examination);
						// Persist Serie in Shanoir DB
						DatasetAcquisition persistedDatasetAcquisition = datasetAcquisitionRepository.save(datasetAcquisition);
						// Persist Dicom images in Shanoir Pacs
			//			if (persistedDatasetAcquisition != null) {
							dicomPersisterService.persistAllForSerie(serie);
					}
				}
			}
		}
	}

}
