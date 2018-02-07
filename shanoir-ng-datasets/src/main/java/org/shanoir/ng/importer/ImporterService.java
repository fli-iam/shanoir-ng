package org.shanoir.ng.importer;

import java.util.ArrayList;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.datasetacquisition.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionRepository;
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
	private ExaminationService examinationService;

	@Autowired
	private DatasetAcquisitionContext datasetAcquisitionContext;
	
	@Autowired
	private DatasetAcquisitionRepository datasetAcquisitionRepository;
	
	private ImportJob importJob;
	
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
			LOG.error("Unable to find Examination",e);
		}
		if (examination != null) {
			int rank = 0;
			for (Patient patient : importJob.getPatients()) {
				for (Study study : patient.getStudies()) {
					for (Serie serie : study.getSeries() ) {
						if (serie.getSelected()) {
							createDatasetAcquisitionForSerie(serie, rank, examination, importJob);
							rank++;
						}
					}
				}
			}
		}
	}
	
	public void createDatasetAcquisitionForSerie(Serie serie, int rank, Examination examination, ImportJob importJob) {
		if (serie.getModality() != null) {
			datasetAcquisitionContext.setDatasetAcquisitionStrategy(serie.getModality());
			DatasetAcquisition datasetAcquisition = datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, rank, importJob);
			datasetAcquisition.setExamination(examination);
			datasetAcquisition.setDatasets(new ArrayList<Dataset>());
			datasetAcquisitionRepository.save(datasetAcquisition);
		}
	}

}
