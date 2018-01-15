package org.shanoir.ng.importer;

import org.shanoir.ng.datasetacquisition.DatasetAcquisitionServiceImpl;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Patients;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ImporterService {
	
	private Patients patients;
	
	@Autowired
	private DatasetAcquisitionServiceImpl datasetAcquisitionService;
	
	public void setPatients(Patients patients) {
		this.patients = patients;
	}

	public Patients retrieveMetadataInDicom() {
		return null;
	}
	
	public void buildDatasets() {
		
	}
	
	public void applyStudyCard() {
		// TODO Implement MrDatasetAcquisitionHome ->  processStudyCardChange(final string folderpath) method
	}
	
	public void createAllDatasetAcquisition() {
		for (Patient patient : patients.patients) {
			for (Study study : patient.studies) {
				for (Serie serie : study.series ) {
					if (serie.getSelected()) {
						createSingleDatasetAcquisition(serie);
					}
				}
			}
		}
	}
	
	public void createSingleDatasetAcquisition(Serie serie) {
		datasetAcquisitionService.createDatasetAcquisition(serie);
	}
}
