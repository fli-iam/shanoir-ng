package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dataset.modality.GenericDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.DatasetsWrapper;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.strategies.dataset.DatasetStrategy;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.studycard.service.StudyCardProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericDatasetAcquisitionStrategy implements DatasetAcquisitionStrategy {
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(GenericDatasetAcquisitionStrategy.class);

	@Autowired
	private DicomProcessing dicomProcessing;
	
	@Autowired
	private StudyCardProcessingService studyCardProcessingService;
	
	@Autowired
	private DatasetStrategy<GenericDataset> datasetStrategy;

	@Autowired
	private StudyCardRepository studyCardRepository;

	
	@Override
	public DatasetAcquisition generateDatasetAcquisitionForSerie(Serie serie, int rank, ImportJob importJob) throws Exception {
		GenericDatasetAcquisition datasetAcquisition = new GenericDatasetAcquisition();
		LOG.info("Generating DatasetAcquisition for   : {} - {} - Rank:{}",serie.getSequenceName(), serie.getProtocolName(), rank);
		Attributes dicomAttributes = null;
		try {
			dicomAttributes = dicomProcessing.getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie(), false);
		} catch (IOException e) {
			LOG.error("Unable to retrieve dicom attributes in file " + serie.getFirstDatasetFileForCurrentSerie().getPath(),e);
		}
		datasetAcquisition.setRank(rank);
		importJob.getProperties().put(ImportJob.RANK_PROPERTY, String.valueOf(rank));
		datasetAcquisition.setSortingIndex(serie.getSeriesNumber());
		datasetAcquisition.setSoftwareRelease(dicomAttributes.getString(Tag.SoftwareVersions));
		StudyCard studyCard = null;
		if (importJob.getStudyCardId() != null) {
			studyCard = getStudyCard(importJob.getStudyCardId());
			datasetAcquisition.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		} else {
			LOG.warn("No studycard given for this import");
		}

		DatasetsWrapper<GenericDataset> datasetsWrapper = datasetStrategy.generateDatasetsForSerie(dicomAttributes, serie, importJob);
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
		Optional<StudyCard> studyCard = studyCardRepository.findById(studyCardId);
		if (!studyCard.isPresent()) {
			throw new IllegalArgumentException("No study card found with id " + studyCardId);
		}
		if (studyCard.get().getAcquisitionEquipmentId() == null) {
			throw new IllegalArgumentException("No acq eq id found for the study card " + studyCardId);
		}
		return studyCard.get();
	}

}
