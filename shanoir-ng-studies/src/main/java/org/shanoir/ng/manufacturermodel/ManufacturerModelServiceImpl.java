package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.utils.Utils;
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
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerModelServiceImpl implements ManufacturerModelService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ManufacturerModelServiceImpl.class);

	@Autowired
	private ManufacturerModelRepository manufacturerModelRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public List<ManufacturerModel> findAll() {
		return Utils.toList(manufacturerModelRepository.findAll());
	}

	@Override
	public List<ManufacturerModel> findBy(String fieldName, Object value) {
		return manufacturerModelRepository.findBy(fieldName, value);
	}

	@Override
	public ManufacturerModel findById(final Long id) {
		return manufacturerModelRepository.findOne(id);
	}

	@Override
	public ManufacturerModel save(final ManufacturerModel manufacturerModel) throws ShanoirStudiesException {
		ManufacturerModel savedManufacturerModel = null;
		try {
			savedManufacturerModel = manufacturerModelRepository.save(manufacturerModel);
		} catch (DataIntegrityViolationException dive) {
			ShanoirStudiesException.logAndThrow(LOG, "Error while creating manufacturer model: " + dive.getMessage());
		}
		updateShanoirOld(savedManufacturerModel);
		return savedManufacturerModel;
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param manufacturerModel manufacturer model.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final ManufacturerModel manufacturerModel) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.manufacturerModelQueueOut().getName(),
					new ObjectMapper().writeValueAsString(manufacturerModel));
			return true;
		} catch (AmqpException e) {
			LOG.error(
					"Cannot send manufacturer model " + manufacturerModel.getId()
							+ " save/update to Shanoir Old on queue : " + RabbitMqConfiguration.queueOut().getName(),
					e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send manufacturer model " + manufacturerModel.getId()
					+ " save/update because of an error while serializing manufacturer model.", e);
		}
		return false;
	}

}
