package org.shanoir.ng.center;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirCenterException;
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
public class CenterServiceImpl implements CenterService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CenterServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private CenterRepository centerRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirCenterException {
		centerRepository.delete(id);
	}

	@Override
	public List<Center> findAll() {
		return Utils.toList(centerRepository.findAll());
	}

	@Override
	public List<Center> findBy(final String fieldName, final Object value) {
		return centerRepository.findBy(fieldName, value);
	}

	@Override
	public Center findById(final Long id) {
		return centerRepository.findOne(id);
	}

	@Override
	public Center save(final Center center) throws ShanoirCenterException {
		Center savedCenter = null;
		try {
			savedCenter = centerRepository.save(center);
		} catch (DataIntegrityViolationException dive) {
			ShanoirCenterException.logAndThrow(LOG, "Error while creating center: " + dive.getMessage());
		}
		updateShanoirOld(savedCenter);
		return savedCenter;
	}

	@Override
	public Center update(final Center center) throws ShanoirCenterException {
		final Center centerDb = centerRepository.findOne(center.getId());
		updateCenterValues(centerDb, center);
		try {
			centerRepository.save(centerDb);
		} catch (Exception e) {
			ShanoirCenterException.logAndThrow(LOG, "Error while updating center: " + e.getMessage());
		}
		updateShanoirOld(centerDb);
		return centerDb;
	}

	@Override
	public void updateFromShanoirOld(final Center center) throws ShanoirCenterException {
		if (center.getId() == null) {
			throw new IllegalArgumentException("center id cannot be null");
		} else {
			final Center centerDb = centerRepository.findOne(center.getId());
			if (centerDb != null) {
				try {
					centerRepository.save(centerDb);
				} catch (Exception e) {
					ShanoirCenterException.logAndThrow(LOG,
							"Error while updating center from Shanoir Old: " + e.getMessage());
				}
			}
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param center center.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final Center center) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(),
					new ObjectMapper().writeValueAsString(center));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send center " + center.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send center " + center.getId() + " save/update because of an error while serializing center.",
					e);
		}
		return false;
	}

	/*
	 * Update some values of center to save them in database.
	 * 
	 * @param centerDb center found in database.
	 * 
	 * @param center center with new values.
	 * 
	 * @return database center with new values.
	 */
	private Center updateCenterValues(final Center centerDb, final Center center) {
		centerDb.setName(center.getName());
		centerDb.setStreet(center.getStreet());
		centerDb.setPostalCode(center.getPostalCode());
		centerDb.setCity(center.getCity());
		centerDb.setCountry(center.getCountry());
		centerDb.setPhoneNumber(center.getPhoneNumber());
		centerDb.setWebsite(center.getWebsite());
		return centerDb;
	}

	/* (non-Javadoc)
	 * @see org.shanoir.ng.service.CenterService#findByData(java.lang.String)
	 */
	@Override
	public Optional<Center> findByName(String name) {
		return centerRepository.findByName(name);
	}

}
