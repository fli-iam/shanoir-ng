package org.shanoir.ng.dataset.modality;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.dataset.DatasetService;
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
public class MrDatasetServiceImpl implements DatasetService<MrDataset> {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MrDatasetServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private MrDatasetRepository datasetRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirDatasetsException {
		datasetRepository.delete(id);
	}

	@Override
	public List<MrDataset> findBy(final String fieldName, final Object value) {
		return datasetRepository.findBy(fieldName, value);
	}

	@Override
	public MrDataset findById(final Long id) {
		return datasetRepository.findOne(id);
	}

	@Override
	public MrDataset save(final MrDataset dataset) throws ShanoirDatasetsException {
		MrDataset savedDataset = null;
		try {
			savedDataset = datasetRepository.save(dataset);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating dataset", dive);
			throw new ShanoirDatasetsException("Error while creating dataset");
		}
		updateShanoirOld(savedDataset);
		return savedDataset;
	}

	@Override
	public MrDataset update(final MrDataset dataset) throws ShanoirDatasetsException {
		final MrDataset datasetDb = datasetRepository.findOne(dataset.getId());
		updateDatasetValues(datasetDb, dataset);
		try {
			datasetRepository.save(datasetDb);
		} catch (Exception e) {
			LOG.error("Error while updating dataset", e);
			throw new ShanoirDatasetsException("Error while updating dataset");
		}
		updateShanoirOld(datasetDb);
		return datasetDb;
	}

	@Override
	public void updateFromShanoirOld(final MrDataset dataset) throws ShanoirDatasetsException {
		if (dataset.getId() == null) {
			throw new IllegalArgumentException("Template id cannot be null");
		} else {
			final MrDataset datasetDb = datasetRepository.findOne(dataset.getId());
			if (datasetDb != null) {
				try {
//					datasetDb.setData(dataset.getData());
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
