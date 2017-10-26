package org.shanoir.ng.dataset;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirDatasetException;
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
public class DatasetServiceImpl implements DatasetService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private DatasetRepository datasetRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirDatasetException {
		datasetRepository.delete(id);
	}

	@Override
	public List<Dataset> findBy(final String fieldName, final Object value) {
		return datasetRepository.findBy(fieldName, value);
	}

	@Override
	public Dataset findById(final Long id) {
		return datasetRepository.findOne(id);
	}

	@Override
	public Dataset save(final Dataset dataset) throws ShanoirDatasetException {
		Dataset savedDataset = null;
		try {
			savedDataset = datasetRepository.save(dataset);
		} catch (DataIntegrityViolationException dive) {
			ShanoirDatasetException.logAndThrow(LOG, "Error while creating template: " + dive.getMessage());
		}
		updateShanoirOld(savedDataset);
		return savedDataset;
	}

	@Override
	public Dataset update(final Dataset dataset) throws ShanoirDatasetException {
		final Dataset datasetDb = datasetRepository.findOne(dataset.getId());
		updateDatasetValues(datasetDb, dataset);
		try {
			datasetRepository.save(datasetDb);
		} catch (Exception e) {
			ShanoirDatasetException.logAndThrow(LOG, "Error while updating dataset: " + e.getMessage());
		}
		updateShanoirOld(datasetDb);
		return datasetDb;
	}

	@Override
	public void updateFromShanoirOld(final Dataset dataset) throws ShanoirDatasetException {
		if (dataset.getId() == null) {
			throw new IllegalArgumentException("Template id cannot be null");
		} else {
			final Dataset datasetDb = datasetRepository.findOne(dataset.getId());
			if (datasetDb != null) {
				try {
//					datasetDb.setData(dataset.getData());
					datasetRepository.save(datasetDb);
				} catch (Exception e) {
					ShanoirDatasetException.logAndThrow(LOG,
							"Error while updating dataset from Shanoir Old: " + e.getMessage());
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
			LOG.error("Cannot send dataset " + dataset.getId() + " save/update because of an error while serializing dataset.",
					e);
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
		datasetDb.setName(dataset.getName());
		return datasetDb;
	}

}
