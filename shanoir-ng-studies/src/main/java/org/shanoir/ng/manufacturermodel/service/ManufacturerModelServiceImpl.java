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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerModelServiceImpl extends BasicEntityServiceImpl<ManufacturerModel> implements ManufacturerModelService {
	
	@Autowired
	private ManufacturerModelRepository manufacturerModelRepository;

	@Autowired
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public List<IdName> findIdsAndNames() {
		return manufacturerModelRepository.findIdsAndNames();
	}

	@Override
	public List<IdName> findIdsAndNamesForCenter(Long centerId) {
		return manufacturerModelRepository.findIdsAndNames();
	}

	@Override
	protected ManufacturerModel updateValues(ManufacturerModel from, ManufacturerModel to) {
		to.setDatasetModalityType(from.getDatasetModalityType());
		to.setMagneticField(from.getMagneticField());
		to.setManufacturer(from.getManufacturer());
		to.setName(from.getName());

		try {
			updateManufacturerModelName(from);
		} catch (MicroServiceCommunicationException e) {
			throw new RuntimeException(e);
		}

		return to;
	}

	private boolean updateManufacturerModelName(ManufacturerModel manufacturerModel) throws MicroServiceCommunicationException {
		try {
			String manuModelName = manufacturerModel.getName();
			List<AcquisitionEquipment> listAcEq = acquisitionEquipmentRepository.findByManufacturerModelId(manufacturerModel.getId());
			for (AcquisitionEquipment acEqItem : listAcEq) {
				IdName acEq = new IdName();
				acEq.setId(acEqItem.getId());
				acEq.setName(acEqItem.getManufacturerModel().getManufacturer().getName() + " " + manuModelName);
					rabbitTemplate.convertAndSend(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_UPDATE_QUEUE,
						objectMapper.writeValueAsString(acEq));
			}

			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException(
					"Error while communicating with datasets MS to update manufacturer model name.", e);
		}
	}
}
