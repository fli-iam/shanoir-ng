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

package org.shanoir.ng.center.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CenterServiceImpl extends BasicEntityServiceImpl<Center> implements CenterService {

	@Autowired
	private CenterRepository centerRepository;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private static final Logger LOG = LoggerFactory.getLogger(CenterServiceImpl.class);
	
	@Override
	public void deleteByIdCheckDependencies(final Long id) throws EntityNotFoundException, UndeletableDependenciesException {
		final Optional<Center> centerOpt = centerRepository.findById(id);
		if (centerOpt.isEmpty()) {
			throw new EntityNotFoundException(Center.class, id);
		}
		final List<FieldError> errors = new ArrayList<>();
		if (!centerOpt.get().getAcquisitionEquipments().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "acquisitionEquipments"));
		}
		if (!centerOpt.get().getStudyCenterList().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "studies"));
		}
		if (!errors.isEmpty()) {
			final FieldErrorMap errorMap = new FieldErrorMap();
			errorMap.put("delete", errors);
			throw new UndeletableDependenciesException(errorMap);
		}
		centerRepository.deleteById(id);
	}

	@Override
	public List<IdName> findIdsAndNames() {
		return centerRepository.findIdsAndNames();
	}
	
	@Override
	public List<IdName> findIdsAndNames(Long studyId) {
		return centerRepository.findIdsAndNames(studyId);
	}

	@Override
	protected Center updateValues(final Center from, final Center to) {
		to.setCity(from.getCity());
		to.setCountry(from.getCountry());
		to.setName(from.getName());
		to.setPhoneNumber(from.getPhoneNumber());
		to.setPostalCode(from.getPostalCode());
		to.setStreet(from.getStreet());
		to.setWebsite(from.getWebsite());
		return to;
	}
	
	@Override
	public Center update(Center center) throws EntityNotFoundException {		
		final Center centerDb = centerRepository.findOne(center.getId());
		if (centerDb == null) {
			throw new EntityNotFoundException(center.getClass(), center.getId());
		}
		String previousName = centerDb.getName();
		updateValues(center, centerDb);
		Center updatedCenter = centerRepository.save(centerDb);
		// send name update via rabbitmq
		if (!previousName.equals(updatedCenter.getName())) {
			try {
				updateName(new IdName(center.getId(), center.getName()));
			} catch (MicroServiceCommunicationException e) {
				LOG.error("Could not send the center name change to the other microservices !", e);
			}
		}
		return updatedCenter;
	}
	
	@Override
	public Center create(Center center) {
		try {
			updateName(new IdName(center.getId(), center.getName()));
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not send the center name creation to the other microservices !", e);
		}
		return super.create(center);
	}

	@Override
	public Center findByName(String name) {
		return centerRepository.findByName(name);
	}
	
	private boolean updateName(IdName idName) throws MicroServiceCommunicationException{
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.CENTER_NAME_UPDATE_QUEUE,
					new ObjectMapper().writeValueAsString(idName));
			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Error while communicating with datasets MS to update center name.");
		}
	}
}
