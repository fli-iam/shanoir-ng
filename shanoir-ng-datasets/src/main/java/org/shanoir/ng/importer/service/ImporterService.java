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

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;

import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ProcessedDatasetType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.eeg.EegDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ImporterService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ImporterService.class);
	
	private static final String UPLOAD_EXTENSION = ".upload";
		
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
	
	public void cleanTempFiles(String workFolder) {
		
		if (workFolder != null) {
			// delete workFolder.upload file
			File uploadZipFile = new File(workFolder.concat(UPLOAD_EXTENSION));
			uploadZipFile.delete();
			// delete workFolder
			final boolean success = Utils.deleteFolder((new File(workFolder)));
			if (!success) {
				if (new File(workFolder).exists()) {
					LOG.error("cleanTempFiles: " + workFolder + " could not be deleted" );
				} else {
					LOG.error("cleanTempFiles: " + workFolder + " does not exist" );
				}
			}
		} else {
			LOG.error("cleanTempFiles: workFolder is null");
		}
	}

	/**
	 * Create a dataset acquisition, and associated dataset.
	 * @param importJob the import job from importer MS.
	 */
	public void createEegDataset(EegImportJob importJob) {
		DatasetAcquisition datasetAcquisition = new EegDatasetAcquisition();
		
		// Get examination
		Examination examination = examinationService.findById(importJob.getExaminationId());

		datasetAcquisition.setExamination(examination);
		datasetAcquisition.setAcquisitionEquipmentId(importJob.getFrontAcquisitionEquipmentId());
		datasetAcquisition.setRank(Integer.valueOf(1));

		// Metadata
		DatasetMetadata originMetadata = new DatasetMetadata();
		originMetadata.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
		originMetadata.setDatasetModalityType(DatasetModalityType.EEG_DATASET);
		originMetadata.setName("Nom temporaire");
		originMetadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);
		
		// Create the dataset with informations from job
		EegDataset datasetToCreate = new EegDataset();

		// set the dataset_Id where needed
		for (Channel chan : importJob.getChannels()) {
			chan.setDataset(datasetToCreate);
		}
		for (Event event : importJob.getEvents()) {
			event.setDataset(datasetToCreate);
		}
		datasetToCreate.setChannelCount(importJob.getChannels() != null? importJob.getChannels().size() : 0);
		datasetToCreate.setChannelList(importJob.getChannels());
		datasetToCreate.setEventList(importJob.getEvents());
		datasetToCreate.setCreationDate(LocalDate.now());
		datasetToCreate.setDatasetAcquisition(datasetAcquisition);
		datasetToCreate.setOriginMetadata(originMetadata);
		datasetToCreate.setStudyId(importJob.getFrontStudyId());
		datasetToCreate.setSubjectId(importJob.getSubjectId());

		datasetAcquisition.setDatasets(Collections.singletonList(datasetToCreate));
		datasetAcquisitionRepository.save(datasetAcquisition);
	}

}
