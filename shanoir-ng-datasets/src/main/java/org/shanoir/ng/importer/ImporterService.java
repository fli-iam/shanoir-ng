package org.shanoir.ng.importer;

import org.shanoir.ng.dataset.DatasetApiController;
import org.shanoir.ng.datasetacquisition.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.DatasetAcquisitionServiceImpl;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(DatasetApiController.class);
	
	@Autowired
	ExaminationService examinationService;
	
	@Autowired
	DatasetAcquisition datasetAcquisition;
	
	private ImportJob importJob;
	
	@Autowired
	private DatasetAcquisitionServiceImpl datasetAcquisitionServiceImpl;
	
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
			for (Patient patient : importJob.getPatients().patients) {
				for (Study study : patient.studies) {
					for (Serie serie : study.series ) {
						if (serie.getSelected()) {
							createSingleDatasetAcquisition(serie,rank,examination);
							rank++;
						}
					}
				}
			}
		}
	}
	
	public void createSingleDatasetAcquisition(Serie serie, int rank, Examination examination) {
		datasetAcquisition.setExamination(examination);
		datasetAcquisition.setRank(rank);
		datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
//		datasetAcquisition.setSoftwareRelease(softwareRelease);
//		datasetAcquisition.setAcquisitionEquipmentId(acquisitionEquipmentId);
//		datasetAcquisition.setDatasets(datasets);
		
	
		try {
			datasetAcquisitionServiceImpl.save(datasetAcquisition);
		} catch (ShanoirException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
