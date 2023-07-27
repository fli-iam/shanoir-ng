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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerRepository;
import org.shanoir.ng.messaging.StudyUserUpdateBroadcastService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerServiceImpl extends BasicEntityServiceImpl<Manufacturer> implements ManufacturerService {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AcquisitionEquipmentRepository acquisitionEquipmentRepository;
	@Autowired
	private ManufacturerModelRepository manufacturerModelRepository;

	@Autowired
	private StudyUserUpdateBroadcastService studyUserUpdateBroadcastService;

	@Override
	protected Manufacturer updateValues(Manufacturer manu, Manufacturer manuDb) {
		System.out.println("manufacturer update values from : " + manuDb.getName() + " / to : " + manu.getName());
		manuDb.setName(manu.getName());

		try {
			updateManufacturerName(manu);
		} catch (MicroServiceCommunicationException e) {
			throw new RuntimeException(e);
		}
		return manuDb;
	}

	private boolean updateManufacturerName(Manufacturer manufacturer) throws MicroServiceCommunicationException {
		try {
			System.out.println("Manu == updateManufacturerName : " + manufacturer.getName());

			String manuName = manufacturer.getName();
			List<ManufacturerModel> listManuModel = manufacturerModelRepository.findByManufacturerId(manufacturer.getId());
			for (ManufacturerModel manuModel : listManuModel) {
				System.out.println("Manu == manuModel : " + manuModel.getName());
				List<AcquisitionEquipment> listAcEq = acquisitionEquipmentRepository.findByManufacturerModelId(manuModel.getId());
				for (AcquisitionEquipment acEqItem : listAcEq) {
					System.out.println("Manu == acEq manumodel : " + acEqItem.getManufacturerModel().getName());
					IdName acEq = new IdName();
					acEq.setId(acEqItem.getId());
					acEq.setName(manuName.trim() + " " + acEqItem.getManufacturerModel().getName());
					System.out.println("Manu == acEqDb name : " + acEq.getId() + " / " + acEq.getName());
					System.out.println("Manu == objectMapper.writeValueAsString(acEq) : " + objectMapper.writeValueAsString(acEq));
					rabbitTemplate.convertAndSend(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_UPDATE_QUEUE,
							objectMapper.writeValueAsString(acEq));
					System.out.println("Manu == sent");
				}
			}

			return true;
		} catch (AmqpException | JsonProcessingException e) {
			throw new MicroServiceCommunicationException(
					"Error while communicating with datasets MS to update manufacturer name.", e);
		}
	}
}
