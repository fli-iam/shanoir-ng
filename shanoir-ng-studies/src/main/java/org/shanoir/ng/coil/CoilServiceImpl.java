package org.shanoir.ng.coil;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ErrorModelCode;
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
 * center service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class CoilServiceImpl implements CoilService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CoilServiceImpl.class);

	@Autowired
	private CoilRepository coilRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void deleteById(final Long id) throws ShanoirStudiesException {
		final Coil coil = coilRepository.findOne(id);
		if (coil == null) {
			LOG.error("Coil with id " + id + " not found");
			throw new ShanoirStudiesException(ErrorModelCode.COIL_NOT_FOUND);
		}
		coilRepository.delete(id);
		deleteCoilOnShanoirOld(id);
	}

	@Override
	public List<Coil> findAll() {
		return Utils.toList(coilRepository.findAll());
	}

	@Override
	public List<Coil> findBy(final String fieldName, final Object value) {
		return coilRepository.findBy(fieldName, value);
	}

	@Override
	public Coil findById(final Long id) {
		return coilRepository.findOne(id);
	}

	@Override
	public Coil save(final Coil center) throws ShanoirStudiesException {
		Coil savedCoil = null;
		try {
			savedCoil = coilRepository.save(center);
		} catch (DataIntegrityViolationException dive) {
			ShanoirStudiesException.logAndThrow(LOG, "Error while creating coil: " + dive.getMessage());
		}
		updateShanoirOld(savedCoil);
		return savedCoil;
	}

	@Override
	public Coil update(final Coil center) throws ShanoirStudiesException {
		final Coil coilDb = coilRepository.findOne(center.getId());
		updateCenterValues(coilDb, center);
		try {
			coilRepository.save(coilDb);
		} catch (Exception e) {
			ShanoirStudiesException.logAndThrow(LOG, "Error while updating coil: " + e.getMessage());
		}
		updateShanoirOld(coilDb);
		return coilDb;
	}

	@Override
	public void updateFromShanoirOld(final Coil coil) throws ShanoirStudiesException {
		if (coil.getId() == null) {
			throw new IllegalArgumentException("coil id cannot be null");
		} else {
			final Coil centerDb = coilRepository.findOne(coil.getId());
			if (centerDb != null) {
				try {
					coilRepository.save(centerDb);
				} catch (Exception e) {
					ShanoirStudiesException.logAndThrow(LOG,
							"Error while updating coil from Shanoir Old: " + e.getMessage());
				}
			}
		}
	}

	/*
	 * Send a message to Shanoir old to delete a center.
	 * 
	 * @param centerId center id.
	 */
	private void deleteCoilOnShanoirOld(final Long centerId) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.deleteCenterQueueOut().getName(),
					new ObjectMapper().writeValueAsString(centerId));
		} catch (AmqpException e) {
			LOG.error("Cannot send center " + centerId + " delete to Shanoir Old on queue : "
					+ RabbitMqConfiguration.deleteCenterQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send center " + centerId + " because of an error while serializing center.", e);
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param center center.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final Coil coil) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.centerQueueOut().getName(),
					new ObjectMapper().writeValueAsString(coil));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send coil " + coil.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.centerQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send coil " + coil.getId()
					+ " save/update because of an error while serializing coil.", e);
		}
		return false;
	}

	/*
	 * Update some values of coil to save them in database.
	 * 
	 * @param coilDb coil found in database.
	 * 
	 * @param coil coil with new values.
	 * 
	 * @return database coil with new values.
	 */
	private Coil updateCenterValues(final Coil coilDb, final Coil coil) {
		coilDb.setCoilType(coil.getCoilType());
		coilDb.setName(coil.getName());
		coilDb.setNumberOfChannels(coil.getNumberOfChannels());
		coilDb.setSerialNumber(coil.getSerialNumber());
		return coilDb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.ng.service.CenterService#findByData(java.lang.String)
	 */
	@Override
	public Optional<Coil> findByName(String name) {
		return coilRepository.findByName(name);
	}

}
