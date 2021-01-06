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
package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetProtocol;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.shanoir.ng.importer.strategies.protocol.PetProtocolStrategy;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.studycard.service.StudyCardProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyao
 *
 */
@Component
public class PetDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy{


	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(PetDatasetAcquisitionStrategy.class);
	
	@Autowired
	private DicomProcessing dicomProcessing;
	
	@Autowired
	private PetProtocolStrategy protocolStrategy;
	
	@Autowired 
	private DatasetStrategy<PetDataset> datasetStrategy;
	
	@Autowired
	private StudyCardProcessingService studyCardProcessingService;

	@Autowired
	private StudyCardRepository studyCardRepository;

	
	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob)
			throws Exception {
		
		PetDatasetAcquisition datasetAcquisition = new PetDatasetAcquisition();
		LOG.info("Generating DatasetAcquisition for   : {} - {} - Rank:{}", serie.getSequenceName(), serie.getProtocolName(), rank);
		Attributes dicomAttributes = null;
		try {
			// TODO ATO : should always be a dicom: add check
			dicomAttributes = dicomProcessing.getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie(), false);
		} catch (IOException e) {
			LOG.error("Unable to retrieve dicom attributes in file " + serie.getFirstDatasetFileForCurrentSerie().getPath(),e);
		}
		datasetAcquisition.setRank(rank);
		datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		datasetAcquisition.setSoftwareRelease(dicomAttributes.getString(Tag.SoftwareVersions));
		StudyCard studyCard = null;
		if (importJob.getStudyCardId() != null) {
			studyCard = getStudyCard(importJob.getStudyCardId());
			datasetAcquisition.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());			
		} else {
			LOG.warn("No studycard given for this import");
		}
		PetProtocol protocol = protocolStrategy.generateProtocolForSerie(dicomAttributes);
		datasetAcquisition.setPetProtocol(protocol);
	
		// TODO ATO add Compatibility check between study card Equipment and dicomEquipment if not done at front level.
		DatasetsWrapper<PetDataset> datasetsWrapper = datasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, importJob);
		List<Dataset> genericizedList = new ArrayList<>();
		for (Dataset dataset : datasetsWrapper.getDatasets()) {
			dataset.setDatasetAcquisition(datasetAcquisition);
			genericizedList.add(dataset);
		}
		datasetAcquisition.setDatasets(genericizedList);
		
		if (studyCard != null) {
			studyCardProcessingService.applyStudyCard(datasetAcquisition, studyCard, dicomAttributes);			
		}
		
		return datasetAcquisition;
	}
	
	private StudyCard getStudyCard(Long studyCardId) {
		StudyCard studyCard = studyCardRepository.findOne(studyCardId);
		if (studyCard == null) throw new IllegalArgumentException("No study card found with id " + studyCardId);
		if (studyCard.getAcquisitionEquipmentId() == null) throw new IllegalArgumentException("No acq eq id found for the study card " + studyCardId);
		return studyCard;
	}

}
