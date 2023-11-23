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

package org.shanoir.ng.acquisitionequipment.service;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.service.CenterServiceImpl;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Acquisition equipment service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class AcquisitionEquipmentServiceImpl implements AcquisitionEquipmentService {

	@Autowired
	private AcquisitionEquipmentRepository repository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private static final Logger LOG = LoggerFactory.getLogger(AcquisitionEquipmentServiceImpl.class);
	@Override
	public Optional<AcquisitionEquipment> findById(final Long id) {
		return repository.findById(id);
	}

	protected AcquisitionEquipment updateValues(AcquisitionEquipment from, AcquisitionEquipment to) {
		to.setCenter(from.getCenter());
		to.setManufacturerModel(from.getManufacturerModel());
		to.setSerialNumber(from.getSerialNumber());
		return to;
	}

	public List<AcquisitionEquipment> findAll() {
		return Utils.toList(repository.findAll());
	}

	public List<AcquisitionEquipment> findAllByCenterId(Long centerId) {
		return this.repository.findByCenterId(centerId);
	}

	public List<AcquisitionEquipment> findAllByStudyId(Long studyId) {
		return this.repository.findByCenterStudyCenterListStudyId(studyId);
	}

	public AcquisitionEquipment create(AcquisitionEquipment entity) {
		AcquisitionEquipment newDbAcEq = repository.save(entity);
		try {
			updateName(newDbAcEq);
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not send the center name creation to the other microservices !", e);
		}
		return newDbAcEq;
	}

	private boolean updateName(AcquisitionEquipment equipment) throws MicroServiceCommunicationException{
		try {
			String datasetAcEqName =
					equipment.getManufacturerModel().getManufacturer().getName() + " - "
							+ equipment.getManufacturerModel().getName() + " "
							+ (equipment.getManufacturerModel().getMagneticField() != null ? (equipment.getManufacturerModel().getMagneticField() + "T ") : "")
							+ equipment.getSerialNumber() + " - " + equipment.getCenter().getName();

			rabbitTemplate.convertAndSend(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_UPDATE_QUEUE,
					objectMapper.writeValueAsString(new IdName(equipment.getId(), datasetAcEqName)));
			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Error while communicating with datasets MS to update acquisition equipment name.");
		}
	}

	public AcquisitionEquipment update(final AcquisitionEquipment entity) throws EntityNotFoundException {
		final Optional<AcquisitionEquipment> entityDbOpt = repository.findById(entity.getId());
		final AcquisitionEquipment entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(entity.getClass(), entity.getId()));
		AcquisitionEquipment updated = updateValues(entity, entityDb);
		try {
			updateName(updated);
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not send the center name creation to the other microservices !", e);
		}		return repository.save(entityDb);
	}

	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<AcquisitionEquipment> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}

}
