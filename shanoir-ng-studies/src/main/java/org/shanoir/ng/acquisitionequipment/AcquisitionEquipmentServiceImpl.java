package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
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
 * Acquisition equipment service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class AcquisitionEquipmentServiceImpl implements AcquisitionEquipmentService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AcquisitionEquipmentServiceImpl.class);

	@Autowired
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void deleteById(final Long id) throws ShanoirStudiesException {
		final AcquisitionEquipment equipment = acquisitionEquipmentRepository.findOne(id);
		if (equipment == null) {
			LOG.error("Acquisition equipment with id " + id + " not found");
			throw new ShanoirStudiesException(StudiesErrorModelCode.ACQ_EQPT_NOT_FOUND);
		}
		acquisitionEquipmentRepository.delete(id);
		deleteEquipmentOnShanoirOld(id);
	}

	@Override
	public List<AcquisitionEquipment> findAll() {
		return Utils.toList(acquisitionEquipmentRepository.findAll());
	}

	@Override
	public AcquisitionEquipment findById(final Long id) {
		return acquisitionEquipmentRepository.findOne(id);
	}

	@Override
	public AcquisitionEquipment save(final AcquisitionEquipment acquisitionEquipment) throws ShanoirStudiesException {
		AcquisitionEquipment savedEquipment = null;
		try {
			savedEquipment = acquisitionEquipmentRepository.save(acquisitionEquipment);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating acquisition equipment", dive);
			throw new ShanoirStudiesException("Error while creating acquisition equipment");
		}
		updateShanoirOld(savedEquipment);
		return savedEquipment;
	}

	@Override
	public AcquisitionEquipment update(final AcquisitionEquipment acquisitionEquipment) throws ShanoirStudiesException {
		final AcquisitionEquipment equipmentDb = acquisitionEquipmentRepository.findOne(acquisitionEquipment.getId());
		if (equipmentDb == null) {
			LOG.error("Acquisition equipment with id " + acquisitionEquipment.getId() + " not found");
			throw new ShanoirStudiesException(StudiesErrorModelCode.ACQ_EQPT_NOT_FOUND);
		}
		updateEquipmentValues(equipmentDb, acquisitionEquipment);
		try {
			acquisitionEquipmentRepository.save(equipmentDb);
		} catch (Exception e) {
			LOG.error("Error while updating acquisition equipment", e);
			throw new ShanoirStudiesException("Error while updating acquisition equipment");
		}
		updateShanoirOld(equipmentDb);
		return equipmentDb;
	}

	/*
	 * Send a message to Shanoir old to delete an equipment.
	 * 
	 * @param equipmentId equipment id.
	 */
	private void deleteEquipmentOnShanoirOld(final Long equipmentId) {
		try {
			LOG.info("Send delete to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.deleteAcqEqptQueueOut().getName(),
					new ObjectMapper().writeValueAsString(equipmentId));
		} catch (AmqpException e) {
			LOG.error("Cannot send equipment " + equipmentId + " delete to Shanoir Old on queue : "
					+ RabbitMqConfiguration.deleteAcqEqptQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send equipment " + equipmentId + " because of an error while serializing equipment.", e);
		}
	}

	/*
	 * Update some values of equipment to save them in database.
	 * 
	 * @param equipmentDb equipment found in database.
	 * 
	 * @param equipment equipment with new values.
	 * 
	 * @return database equipment with new values.
	 */
	private AcquisitionEquipment updateEquipmentValues(final AcquisitionEquipment equipmentDb,
			final AcquisitionEquipment equipment) {
		equipmentDb.setCenter(equipment.getCenter());
		equipmentDb.setManufacturerModel(equipment.getManufacturerModel());
		equipmentDb.setSerialNumber(equipment.getSerialNumber());
		return equipmentDb;
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param acquisitionEquipment acquisition equipment.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final AcquisitionEquipment acquisitionEquipment) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.acqEqptQueueOut().getName(),
					new ObjectMapper().writeValueAsString(acquisitionEquipment));
			return true;
		} catch (AmqpException e) {
			LOG.error(
					"Cannot send acquisition equipment " + acquisitionEquipment.getId()
							+ " save/update to Shanoir Old on queue : " + RabbitMqConfiguration.acqEqptQueueOut().getName(),
					e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send acquisition equipment " + acquisitionEquipment.getId()
					+ " save/update because of an error while serializing acquisition equipment.", e);
		}
		return false;
	}

	/**
	 * 
	 */
	@Override
	public List<AcquisitionEquipment> findBy(String fieldName, Object value) {
		return acquisitionEquipmentRepository.findBy(fieldName, value);
	}
	
	/**
	 * 
	 */
	@Override
	public List<AcquisitionEquipment> findByCoupleOfFieldValue(String fieldName1, Object value1, String fieldName2, Object value2) {
		return acquisitionEquipmentRepository.findByCoupleOfFieldValue(fieldName1, value1, fieldName2, value2);
	}

}
