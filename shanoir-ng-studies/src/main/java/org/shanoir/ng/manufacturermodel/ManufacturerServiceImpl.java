package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirStudyException;
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
	public Manufacturer save(final Manufacturer manufacturer) throws ShanoirStudyException {
		Manufacturer savedManufacturer = null;
		try {
			savedManufacturer = manufacturerRepository.save(manufacturer);
		} catch (DataIntegrityViolationException dive) {
			ShanoirStudyException.logAndThrow(LOG, "Error while creating acquisition equipment: " + dive.getMessage());
		}
		updateShanoirOld(savedManufacturer);
		return savedManufacturer;
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
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.manufacturerQueueOut().getName(),
					new ObjectMapper().writeValueAsString(manufacturer));
			return true;
		} catch (AmqpException e) {
			LOG.error(
					"Cannot send manufacturer " + manufacturer.getId()
							+ " save/update to Shanoir Old on queue : " + RabbitMqConfiguration.queueOut().getName(),
					e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send manufacturer " + manufacturer.getId()
					+ " save/update because of an error while serializing manufacturer.", e);
		}
		return false;
	}

}
