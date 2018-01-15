package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
import org.shanoir.ng.shared.dto.IdNameDTO;
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
	public ManufacturerModel findById(final Long id) {
		return manufacturerModelRepository.findOne(id);
	}

	@Override
	public ManufacturerModel save(final ManufacturerModel manufacturerModel) throws ShanoirStudiesException {
		ManufacturerModel savedManufacturerModel = null;
		try {
			savedManufacturerModel = manufacturerModelRepository.save(manufacturerModel);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating manufacturer model", dive);
			throw new ShanoirStudiesException("Error while creating manufacturer model");
		}
		updateShanoirOld(savedManufacturerModel);
		return savedManufacturerModel;
	}

	@Override
	public ManufacturerModel update(final ManufacturerModel manufacturerModel) throws ShanoirStudiesException {
		final ManufacturerModel manufacturerModelDb = manufacturerModelRepository.findOne(manufacturerModel.getId());
		if (manufacturerModelDb == null) {
			LOG.error("Manufacturer model with id " + manufacturerModel.getId() + " not found");
			throw new ShanoirStudiesException(StudiesErrorModelCode.MANUFACTURER_MODEL_NOT_FOUND);
		}
		manufacturerModelDb.setDatasetModalityType(manufacturerModel.getDatasetModalityType());
		manufacturerModelDb.setMagneticField(manufacturerModel.getMagneticField());
		manufacturerModelDb.setManufacturer(manufacturerModel.getManufacturer());
		manufacturerModelDb.setName(manufacturerModel.getName());
		try {
			manufacturerModelRepository.save(manufacturerModelDb);
		} catch (Exception e) {
			LOG.error("Error while updating manufacturer model", e);
			throw new ShanoirStudiesException("Error while updating manufacturer model");
		}
		updateShanoirOld(manufacturerModelDb);
		return manufacturerModelDb;
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
			LOG.error("Cannot send manufacturer model " + manufacturerModel.getId()
					+ " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.manufacturerModelQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send manufacturer model " + manufacturerModel.getId()
					+ " save/update because of an error while serializing manufacturer model.", e);
		}
		return false;
	}

	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return manufacturerModelRepository.findIdsAndNames();
	}

}
