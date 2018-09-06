package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
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
 * Manufacturer service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerServiceImpl implements ManufacturerService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ManufacturerModelServiceImpl.class);

	@Autowired
	private ManufacturerRepository manufacturerRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public List<Manufacturer> findAll() {
		return Utils.toList(manufacturerRepository.findAll());
	}

	@Override
	public List<Manufacturer> findBy(String fieldName, Object value) {
		return manufacturerRepository.findBy(fieldName, value);
	}

	@Override
	public Manufacturer findById(final Long id) {
		return manufacturerRepository.findOne(id);
	}

	@Override
	public Manufacturer save(final Manufacturer manufacturer) throws ShanoirStudiesException {
		Manufacturer savedManufacturer = null;
		try {
			savedManufacturer = manufacturerRepository.save(manufacturer);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating manufacturer", dive);
			throw new ShanoirStudiesException("Error while creating manufacturer");
		}
		updateShanoirOld(savedManufacturer);
		return savedManufacturer;
	}

	@Override
	public Manufacturer update(final Manufacturer manufacturer) throws ShanoirStudiesException {
		final Manufacturer manufacturerDb = manufacturerRepository.findOne(manufacturer.getId());
		if (manufacturerDb == null) {
			LOG.error("Manufacturer with id " + manufacturer.getId() + " not found");
			throw new ShanoirStudiesException(StudiesErrorModelCode.MANUFACTURER_NOT_FOUND);
		}
		manufacturerDb.setName(manufacturer.getName());
		try {
			manufacturerRepository.save(manufacturerDb);
		} catch (Exception e) {
			LOG.error("Error while updating manufacturer", e);
			throw new ShanoirStudiesException("Error while updating manufacturer");
		}
		updateShanoirOld(manufacturerDb);
		return manufacturerDb;
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param manufacturer manufacturer.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final Manufacturer manufacturer) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.manufacturerQueueOut().getName(),
					new ObjectMapper().writeValueAsString(manufacturer));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send manufacturer " + manufacturer.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMQConfiguration.manufacturerQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send manufacturer " + manufacturer.getId()
					+ " save/update because of an error while serializing manufacturer.", e);
		}
		return false;
	}

}
