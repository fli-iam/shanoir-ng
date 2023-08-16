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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
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
public class AcquisitionEquipmentServiceImpl extends BasicEntityServiceImpl<AcquisitionEquipment> implements AcquisitionEquipmentService {

	@Autowired
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private static final Logger LOG = LoggerFactory.getLogger(AcquisitionEquipmentServiceImpl.class);
	@Override
	protected AcquisitionEquipment updateValues(AcquisitionEquipment from, AcquisitionEquipment to) {
		to.setCenter(from.getCenter());
		to.setManufacturerModel(from.getManufacturerModel());
		to.setSerialNumber(from.getSerialNumber());
		return to;
	}

	@Override
	public List<AcquisitionEquipment> findAllByCenterId(Long centerId) {
		return this.acquisitionEquipmentRepository.findByCenterId(centerId);
	}
	
	@Override
	public List<AcquisitionEquipment> findAllByStudyId(Long studyId) {
		return this.acquisitionEquipmentRepository.findByCenterStudyCenterListStudyId(studyId);
	}

	@Override
	public AcquisitionEquipment create(AcquisitionEquipment entity) {
		AcquisitionEquipment newDbAcEq = super.create(entity);
		String datasetAcEqName = newDbAcEq.getManufacturerModel().getManufacturer().getName().trim() + " " + newDbAcEq.getManufacturerModel().getName().trim();
		try {
			updateName(new IdName(newDbAcEq.getId(), datasetAcEqName));
		} catch (MicroServiceCommunicationException e) {
			LOG.error("Could not send the center name creation to the other microservices !", e);
		}
		return newDbAcEq;
	}

	private boolean updateName(IdName idName) throws MicroServiceCommunicationException{
		try {
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_UPDATE_QUEUE,
					objectMapper.writeValueAsString(idName));
			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException("Error while communicating with datasets MS to update center name.");
		}
	}
}
