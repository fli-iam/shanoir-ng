package org.shanoir.ng.importer;

import org.shanoir.ng.datasetacquisition.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionFactory;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionService;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionStrategy;
import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.examination.ExaminationService;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ImporterService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ImporterService.class);
	
	@Autowired
	ExaminationService examinationService;
	
	private ImportJob importJob;
	
	@Autowired
	private DatasetAcquisitionService<DatasetAcquisition> datasetAcquisitionService;
	
	public void setImportJob(ImportJob importJob) {
		this.importJob = importJob;
	}
	
	// TODO 
	public void retrieveMetadataInDicom() {
	}
	
	public void buildDatasets() {
		
	}
	
	public void applyStudyCard() {
		// TODO Implement MrDatasetAcquisitionHome ->  processStudyCardChange(final string folderpath) method
	}
	
	public void createAllDatasetAcquisition() {
		Examination examination = null;
		try {
			examination = examinationService.findById(importJob.getExaminationId());
		} catch (ShanoirException e) {
			// TODO Auto-generated catch block
			LOG.error("Unable to find Examination",e);
		}
		if (examination != null) {
			int rank = 0;
			for (Patient patient : importJob.getPatients()) {
				for (Study study : patient.getStudies()) {
					for (Serie serie : study.getSeries() ) {
						if (serie.getSelected()) {
							createDatasetAcquisitionForSerie(serie,rank,examination,importJob);
							rank++;
						}
					}
				}
			}
		}
	}
	
	public void createDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination,ImportJob importJob) {
		if (serie.getModality() != null) {
			DatasetAcquisitionStrategy datasetAcquisitionStrategy = DatasetAcquisitionFactory.getDatasetAcquisitionStrategy(serie.getModality());
			if (datasetAcquisitionStrategy != null ) {
				DatasetAcquisition datasetAcquisition = datasetAcquisitionStrategy.generateDatasetAcquisitionForSerie(serie,rank,importJob);
				datasetAcquisition.setExamination(examination);
				try {
					
					datasetAcquisitionService.save(datasetAcquisition);
				} catch (ShanoirException e) {
					// TODO Auto-generated catch block
					LOG.error("Unable to persist Dataset Acquisition",e);
				}
			}
		}

		
	}
}
