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
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerRepository;
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


@Service
public class ManufacturerServiceImpl implements ManufacturerService {

    @Autowired
    private ManufacturerRepository repository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AcquisitionEquipmentRepository acquisitionEquipmentRepository;

    @Autowired
    private ManufacturerModelRepository manufacturerModelRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ManufacturerServiceImpl.class);

    protected Manufacturer updateValues(Manufacturer manu, Manufacturer manuDb) {
        manuDb.setName(manu.getName());

        try {
            updateManufacturer(manu);
        } catch (MicroServiceCommunicationException e) {
            LOG.error("Could not send the manufacturer values change to the other microservices !", e);
        }
        return manuDb;
    }

    public boolean updateManufacturer(Manufacturer manufacturer) throws MicroServiceCommunicationException {
        try {
            String manuName = manufacturer.getName();
            if (manufacturer.getId() == null) {
                return true;
            }
            List<ManufacturerModel> listManuModel = manufacturerModelRepository.findByManufacturerId(manufacturer.getId()).orElse(null);
            if (listManuModel == null || listManuModel.isEmpty()) {
                return true;
            }
            for (ManufacturerModel manuModel : listManuModel) {
                List<AcquisitionEquipment> listAcEq = acquisitionEquipmentRepository.findByManufacturerModelId(manuModel.getId());
                if (listAcEq != null) {
                    for (AcquisitionEquipment acEqItem : listAcEq) {
                        IdName acEq = new IdName();
                        acEq.setId(acEqItem.getId());
                        acEq.setName(manuName.trim() + " " + acEqItem.getManufacturerModel().getName());
                        rabbitTemplate.convertAndSend(RabbitMQConfiguration.ACQUISITION_EQUIPEMENT_UPDATE_QUEUE,
                                objectMapper.writeValueAsString(acEq));
                    }
                }
            }
            return true;
        } catch (AmqpException | JsonProcessingException e) {
            throw new MicroServiceCommunicationException(
                    "Error while communicating with datasets MS to update manufacturer name.", e);
        }
    }

    public Optional<Manufacturer> findById(final Long id) {
        return repository.findById(id);
    }

    public List<Manufacturer> findAll() {
        return Utils.toList(repository.findAll());
    }

    public Manufacturer create(final Manufacturer entity) {
        Manufacturer savedEntity = repository.save(entity);
        return savedEntity;
    }

    public Manufacturer update(final Manufacturer entity) throws EntityNotFoundException {
        final Optional<Manufacturer> entityDbOpt = repository.findById(entity.getId());
        final Manufacturer entityDb = entityDbOpt.orElseThrow(
                () -> new EntityNotFoundException(entity.getClass(), entity.getId()));
        updateValues(entity, entityDb);
        return repository.save(entityDb);
    }

    public void deleteById(final Long id) throws EntityNotFoundException, EntityLinkedException {
        final Optional<Manufacturer> entity = repository.findById(id);
        entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new EntityLinkedException("Cannot delete entity with id = " + id + " because it is linked to other entities.", e);
        }
    }

}
