package org.shanoir.ng.dataset;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.modality.CtDatasetRepository;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetRepository;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.modality.PetDatasetRepository;
import org.shanoir.ng.shared.exception.ShanoirDatasetsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Autowired
	private RabbitTemplate rabbitTemplate;

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
	public Dataset update(final Dataset dataset) throws ShanoirDatasetsException {
		final Dataset datasetDb = datasetRepository.findOne(dataset.getId());
		updateDatasetValues(datasetDb, dataset);
		try {
			if (datasetDb instanceof CtDataset) {
				ctDatasetRepository.save((CtDataset) datasetDb);
			} else if (datasetDb instanceof MrDataset) {
				mrDatasetRepository.save((MrDataset) datasetDb);
			} else if (datasetDb instanceof PetDataset) {
				petDatasetRepository.save((PetDataset) datasetDb);
			}
		} catch (Exception e) {
			LOG.error("Error while updating dataset", e);
			throw new ShanoirDatasetsException("Error while updating dataset");
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
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.datasetQueueOut().getName(),
					new ObjectMapper().writeValueAsString(dataset));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send dataset " + dataset.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.datasetQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send dataset " + dataset.getId()
					+ " save/update because of an error while serializing dataset.", e);
		}
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
		datasetDb.setStudyId(dataset.getStudyId());
		// TODO: to complete
		return datasetDb;
	}

}
