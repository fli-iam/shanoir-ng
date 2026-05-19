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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.utils.Utils;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for rabbit MQ communications concerning Center.
 * @author fli
 *
 */
@Service
public class RabbitMqCenterService {

    @Autowired
    private AcquisitionEquipmentRepository acquisitionEquipmentService;

    @Autowired
    private ObjectMapper mapper;

    @RabbitListener(queues = RabbitMQConfiguration.ACQUISITION_EQUIPMENT_CENTER_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public String findCenterIdFromAcquisitionEquipment(String message) {
        try {
            AcquisitionEquipment ae = acquisitionEquipmentService.findById(Long.valueOf(message)).orElse(null);
            if (ae == null) {
                return null;
            } else {
                return mapper.writeValueAsString(new IdName(ae.getCenter().getId(), ae.getCenter().getName()));
            }
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = RabbitMQConfiguration.ACQUISITION_EQUIPMENT_CODE_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public String findAcquisitionEquipments(String message) {
        try {
            List<AcquisitionEquipment> aes = Utils.toList(acquisitionEquipmentService.findAll());
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

    @RabbitListener(queues = RabbitMQConfiguration.EQUIPMENT_FROM_CODE_QUEUE, containerFactory = "multipleConsumersFactory")
    @RabbitHandler
    @Transactional
    public Long getEquipmentFromCode(String message) {
        try {
            List<AcquisitionEquipment> equipList = acquisitionEquipmentService.findBySerialNumberContaining(message);
            if (equipList == null || equipList.isEmpty()) {
                return null;
            }
            return equipList.get(0).getId();
        } catch (Exception e) {
            return null;
        }
    }

}
