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

package org.shanoir.ng.manufacturermodel.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityLinkedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
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

	@Autowired
	private ManufacturerModelRepository manufacturerModelRepository;

	@Autowired
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private static final Logger LOG = LoggerFactory.getLogger(ManufacturerModelServiceImpl.class);
	public Optional<ManufacturerModel> findById(final Long id) {
		return manufacturerModelRepository.findById(id);
	}

	@Override
	public List<ManufacturerModel> findAll() {
		return Utils.toList(manufacturerModelRepository.findAll());
	}

	@Override
	public ManufacturerModel create(final ManufacturerModel entity) {
		ManufacturerModel savedEntity = manufacturerModelRepository.save(entity);
		return savedEntity;
	}

	@Override
	public ManufacturerModel update(final ManufacturerModel entity) throws EntityNotFoundException {
		final Optional<ManufacturerModel> entityDbOpt = manufacturerModelRepository.findById(entity.getId());
		final ManufacturerModel entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(entity.getClass(), entity.getId()));

		updateValues(entity, entityDb);
		return manufacturerModelRepository.save(entityDb);
	}

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException, EntityLinkedException {
		final Optional<ManufacturerModel> entity = manufacturerModelRepository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		try {
			manufacturerModelRepository.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new EntityLinkedException("Cannot delete entity with id = " + id + " because it is linked to other entities.", e);
		}
	}

	@Override
	public List<IdName> findIdsAndNames() {
		return manufacturerModelRepository.findIdsAndNames();
	}

	@Override
	public List<IdName> findIdsAndNamesForCenter(Long centerId) {
		return manufacturerModelRepository.findIdsAndNames();
	}

	protected ManufacturerModel updateValues(ManufacturerModel from, ManufacturerModel to) {
		to.setDatasetModalityType(from.getDatasetModalityType());
		to.setMagneticField(from.getMagneticField());
		to.setManufacturer(from.getManufacturer());
		to.setName(from.getName());

		try {
			updateManufacturerModelName(from);
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not send the manufacturer model values change to the other microservices !", e);
		}

		return to;
	}

	private boolean updateManufacturerModelName(ManufacturerModel manufacturerModel) throws MicroServiceCommunicationException {
		try {
			String manuModelName = manufacturerModel.getName();
			List<AcquisitionEquipment> listAcEq = acquisitionEquipmentRepository.findByManufacturerModelId(manufacturerModel.getId());
			if (listAcEq == null) {
				return true;
			}
			for (AcquisitionEquipment acEqItem : listAcEq) {
				IdName acEq = new IdName();
				acEq.setId(acEqItem.getId());
				acEq.setName(acEqItem.getManufacturerModel().getManufacturer().getName() + " " + manuModelName);
				rabbitTemplate.convertAndSend(RabbitMQConfiguration.ACQUISITION_EQUIPMENT_UPDATE_QUEUE, objectMapper.writeValueAsString(acEq));
			}

			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException(
					"Error while communicating with datasets MS to update manufacturer model name.", e);
		}
	}
}
