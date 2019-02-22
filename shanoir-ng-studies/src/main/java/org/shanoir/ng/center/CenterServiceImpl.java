/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.center;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMQConfiguration;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
import org.shanoir.ng.studycenter.StudyCenter;
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
	private CenterMapper centerMapper;

	@Autowired
	private CenterRepository centerRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void deleteById(final Long id) throws ShanoirStudiesException {
		final Center center = centerRepository.findOne(id);
		if (center == null) {
			LOG.error("Center with id " + id + " not found");
			throw new ShanoirStudiesException(StudiesErrorModelCode.CENTER_NOT_FOUND);
		}
		final List<FieldError> errors = new ArrayList<FieldError>();
		if (!center.getAcquisitionEquipments().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "acquisitionEquipments"));
		}
		if (!center.getStudyCenterList().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "studies"));
		}
		if (!errors.isEmpty()) {
			final FieldErrorMap errorMap = new FieldErrorMap();
			errorMap.put("delete", errors);
			throw new ShanoirStudiesException(errorMap);
		}
		centerRepository.delete(id);
		deleteCenterOnShanoirOld(id);
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
	public List<IdNameDTO> findIdsAndNames() {
		return centerRepository.findIdsAndNames();
	}

	@Override
	public Center save(final Center center) throws ShanoirStudiesException {
		Center savedCenter = null;
		try {
			for (final StudyCenter studyCenter: center.getStudyCenterList()) {
				studyCenter.setCenter(center);
			}
			savedCenter = centerRepository.save(center);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating center", dive);
			throw new ShanoirStudiesException("Error while creating center");
		}
		updateShanoirOld(savedCenter);
		return savedCenter;
	}

	@Override
	public Center update(final Center center) throws ShanoirStudiesException {
		final Center centerDb = centerRepository.findOne(center.getId());
		updateCenterValues(centerDb, center);
		try {
			centerRepository.save(centerDb);
		} catch (Exception e) {
			LOG.error("Error while updating center", e);
			throw new ShanoirStudiesException("Error while updating center");
		}
		updateShanoirOld(centerDb);
		return centerDb;
	}

	@Override
	public void updateFromShanoirOld(final Center center) throws ShanoirStudiesException {
		if (center.getId() == null) {
			throw new IllegalArgumentException("center id cannot be null");
		} else {
			final Center centerDb = centerRepository.findOne(center.getId());
			if (centerDb != null) {
				try {
					centerRepository.save(centerDb);
				} catch (Exception e) {
					LOG.error("Error while updating center from Shanoir Old", e);
					throw new ShanoirStudiesException("Error while updating center from Shanoir Old");
				}
			}
		}
	}

	/*
	 * Send a message to Shanoir old to delete a center.
	 * 
	 * @param centerId center id.
	 */
	private void deleteCenterOnShanoirOld(final Long centerId) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.deleteCenterQueueOut().getName(),
					new ObjectMapper().writeValueAsString(centerId));
		} catch (AmqpException e) {
			LOG.error("Cannot send center " + centerId + " delete to Shanoir Old on queue : "
					+ RabbitMQConfiguration.deleteCenterQueueOut().getName(), e);
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
	private boolean updateShanoirOld(final Center center) {
		final CenterDTO centerDTO = centerMapper.centerToCenterDTO(center);
		centerDTO.setAcquisitionEquipments(null);
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.centerQueueOut().getName(),
					new ObjectMapper().writeValueAsString(centerDTO));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send center " + center.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMQConfiguration.centerQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send center " + center.getId()
					+ " save/update because of an error while serializing center.", e);
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
		centerDb.setCity(center.getCity());
		centerDb.setCountry(center.getCountry());
		centerDb.setName(center.getName());
		centerDb.setPhoneNumber(center.getPhoneNumber());
		centerDb.setPostalCode(center.getPostalCode());
		centerDb.setStreet(center.getStreet());
		centerDb.setWebsite(center.getWebsite());
		return centerDb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.ng.service.CenterService#findByData(java.lang.String)
	 */
	@Override
	public Optional<Center> findByName(String name) {
		return centerRepository.findByName(name);
	}

}
