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

package org.shanoir.ng.dataset;

import java.util.List;

import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.modality.CtDatasetRepository;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetRepository;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.modality.PetDatasetRepository;
import org.shanoir.ng.shared.exception.ShanoirDatasetsException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Dataset service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class DatasetServiceImpl implements DatasetService<Dataset> {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

	@Autowired
	private CtDatasetRepository ctDatasetRepository;

	@Autowired
	private DatasetRepository datasetRepository;

	@Autowired
	private MrDatasetRepository mrDatasetRepository;

	@Autowired
	private PetDatasetRepository petDatasetRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirDatasetsException {
		datasetRepository.delete(id);
	}

	@Override
	public Dataset findById(final Long id) {
		return datasetRepository.findOne(id);
	}

	@Override
	public Dataset save(final Dataset dataset) throws ShanoirDatasetsException {
		Dataset savedDataset = null;
		try {
			if (dataset instanceof CtDataset) {
				savedDataset = ctDatasetRepository.save((CtDataset) dataset);
			} else if (dataset instanceof MrDataset) {
				savedDataset = mrDatasetRepository.save((MrDataset) dataset);
			} else if (dataset instanceof PetDataset) {
				savedDataset = petDatasetRepository.save((PetDataset) dataset);
			}
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating dataset", dive);
			throw new ShanoirDatasetsException("Error while creating dataset");
		}
		// updateShanoirOld(savedDataset);
		return savedDataset;
	}

	@Override
	public Dataset update(final Dataset dataset) {
		final Dataset datasetDb = datasetRepository.findOne(dataset.getId());

		updateDatasetValues(datasetDb, dataset);
		if (datasetDb instanceof CtDataset) {
			ctDatasetRepository.save((CtDataset) datasetDb);
		} else if (datasetDb instanceof MrDataset) {
			mrDatasetRepository.save((MrDataset) datasetDb);
		} else if (datasetDb instanceof PetDataset) {
			petDatasetRepository.save((PetDataset) datasetDb);
		}

		// updateShanoirOld(datasetDb);
		return datasetDb;
	}

	@Override
	public void updateFromShanoirOld(final Dataset dataset) throws ShanoirDatasetsException {
		if (dataset.getId() == null) {
			throw new IllegalArgumentException("Template id cannot be null");
		} else {
			final Dataset datasetDb = datasetRepository.findOne(dataset.getId());
			if (datasetDb != null) {
				try {
					// datasetDb.setData(dataset.getData());
					datasetRepository.save(datasetDb);
				} catch (Exception e) {
					LOG.error("Error while updating dataset from Shanoir Old", e);
					throw new ShanoirDatasetsException("Error while updating dataset from Shanoir Old");
				}
			}
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param dataset dataset.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final Dataset dataset) {
//		try {
//			LOG.info("Send update to Shanoir Old");
//			rabbitTemplate.convertAndSend(RabbitMqConfiguration.datasetQueueOut().getName(),
//					new ObjectMapper().writeValueAsString(dataset));
//			return true;
//		} catch (AmqpException e) {
//			LOG.error("Cannot send dataset " + dataset.getId() + " save/update to Shanoir Old on queue : "
//					+ RabbitMqConfiguration.datasetQueueOut().getName(), e);
//		} catch (JsonProcessingException e) {
//			LOG.error("Cannot send dataset " + dataset.getId()
//					+ " save/update because of an error while serializing dataset.", e);
//		}
		return false;
	}

	/*
	 * Update some values of dataset to save them in database.
	 * 
	 * @param datasetDb dataset found in database.
	 * 
	 * @param dataset dataset with new values.
	 * 
	 * @return database dataset with new values.
	 */
	private Dataset updateDatasetValues(final Dataset datasetDb, final Dataset dataset) {
		datasetDb.setCreationDate(dataset.getCreationDate());
		//datasetDb.setDatasetAcquisition(dataset.getDatasetAcquisition());
		//datasetDb.setDatasetExpressions(dataset.getDatasetExpressions());
		//datasetDb.setDatasetProcessing(dataset.getDatasetProcessing());
		//datasetDb.setGroupOfSubjectsId(dataset.getGroupOfSubjectsId());
		datasetDb.setId(dataset.getId());
		//datasetDb.setOriginMetadata(dataset.getOriginMetadata());
		//datasetDb.setProcessings(dataset.getProcessings());
		//datasetDb.setReferencedDatasetForSuperimposition(dataset.getReferencedDatasetForSuperimposition());
		//datasetDb.setReferencedDatasetForSuperimpositionChildrenList(dataset.getReferencedDatasetForSuperimpositionChildrenList());
		datasetDb.setStudyId(dataset.getStudyId());
		datasetDb.setSubjectId(dataset.getSubjectId());
		datasetDb.setUpdatedMetadata(dataset.getUpdatedMetadata());
		return datasetDb;
	}

	@Override
	public List<Dataset> findAll() throws ShanoirException {
		return datasetRepository.findAll();
	}

	@Override
	public Page<Dataset> findPage(final Pageable pageable) throws ShanoirException {
		Page<Dataset> page = datasetRepository.findAll(pageable);
		return page;
	}

}
