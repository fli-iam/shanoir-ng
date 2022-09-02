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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.utils.Utils;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for rabbit MQ communications concerning Center.
 * @author fli
 *
 */
@Service
public class RabbitMqCenterService {

	@Autowired
	private AcquisitionEquipmentRepository acquisitionEquipementService;

	@Autowired
	private ObjectMapper mapper;
	
	@RabbitListener(queues = RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_CENTER_QUEUE)
	@RabbitHandler
	@Transactional
	public Map<Long, Long> findCenterIdsFromAcquisitionEquipements(String message) {
		try {
			String[] longs = message.split(",");
			List<Long> ids = new ArrayList<>();
			for (String id : longs) {
				ids.add(Long.valueOf(id));
			}
			List<AcquisitionEquipment> equipments = Utils.toList(acquisitionEquipementService.findAllById(ids));
			if (CollectionUtils.isEmpty(equipments)) {
				return Collections.emptyMap();
			} else {
				Map<Long, Long> centersMap = new HashMap<>();
				for (AcquisitionEquipment equip : equipments) {
					centersMap.put(equip.getId(), equip.getCenter().getId());
				}
				return centersMap;
			}
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}

	@RabbitListener(queues = RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_CODE_QUEUE)
	@RabbitHandler
	@Transactional
	public String findAcquisitionEquipements(String message) {
		try {
			List<AcquisitionEquipment> aes = Utils.toList(acquisitionEquipementService.findAll());
			Map<String, Long> easMap = new HashMap<>(); 
			for (AcquisitionEquipment ae : aes) {
				if (ae.getSerialNumber() != null) {
					easMap.put(ae.getSerialNumber(), ae.getId());
				}
			}
			return mapper.writeValueAsString(easMap);
		} catch (Exception e) {
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}
}
