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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dataset.DatasetModalityType;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Acquisition equipment service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class AcquisitionEquipmentServiceImpl implements AcquisitionEquipmentService {

	private static final Logger LOG = LoggerFactory.getLogger(AcquisitionEquipmentServiceImpl.class);
	
	@Autowired
	private AcquisitionEquipmentRepository repository;

	@Autowired
	private ManufacturerModelRepository manufacturerModelRepository;

	@Autowired
	private ManufacturerRepository manufacturerRepository;

	@Autowired
	private CenterRepository centerRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

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

	public List<AcquisitionEquipment> findAllBySerialNumber(String serialNumber) {
		return this.repository.findBySerialNumberContaining(serialNumber);
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

	@Override
	public List<AcquisitionEquipment> findAcquisitionEquipmentsOrCreateByEquipmentDicom(
			Long centerId, EquipmentDicom equipmentDicom) {
		// trace all info from DICOM to get an overview of the possibilities in the hospitals and learn from it
		LOG.info("findAcquisitionEquipmentsOrCreateByEquipmentDicom called with: " + equipmentDicom.toString());
		if (equipmentDicom.isComplete()) { // we consider finding/creating the correct equipment is impossible without all values
			AcquisitionEquipment acquisitionEquipment;
			String dicomSerialNumber = equipmentDicom.getDeviceSerialNumber();
			List<AcquisitionEquipment> equipments = findAllBySerialNumber(dicomSerialNumber);
			if (equipments == null || equipments.isEmpty()) {
				// second try: remove spaces and leading zeros
				dicomSerialNumber = Utils.removeLeadingZeroes(dicomSerialNumber.trim());
				equipments = findAllBySerialNumber(dicomSerialNumber);
				// nothing found with device serial number from DICOM
				if (equipments == null || equipments.isEmpty()) {
					equipments = new ArrayList<AcquisitionEquipment>();
					acquisitionEquipment = saveNewAcquisitionEquipment(centerId, equipmentDicom);
					equipments.add(acquisitionEquipment);
				} else {
					matchOrRemoveEquipments(equipmentDicom, equipments);
					if (equipments.isEmpty()) {
						acquisitionEquipment = saveNewAcquisitionEquipment(centerId, equipmentDicom);
						equipments.add(acquisitionEquipment);
					}
				}
			} else {
				matchOrRemoveEquipments(equipmentDicom, equipments);
				if (equipments.isEmpty()) {
					acquisitionEquipment = saveNewAcquisitionEquipment(centerId, equipmentDicom);
					equipments.add(acquisitionEquipment);
				}
			}
			return equipments;
		}
		return null;
	}

	private AcquisitionEquipment saveNewAcquisitionEquipment(Long centerId, EquipmentDicom equipmentDicom) {
		Optional<ManufacturerModel> manufacturerModelOpt = 
	        	manufacturerModelRepository.findByNameIgnoreCase(equipmentDicom.getManufacturerModelName());
		ManufacturerModel manufacturerModel = manufacturerModelOpt.orElseGet(() -> {
			Manufacturer manufacturer = manufacturerRepository
					.findByNameIgnoreCase(equipmentDicom.getManufacturer())
					.orElseGet(() -> {
						Manufacturer newManufacturer = new Manufacturer();
						newManufacturer.setName(equipmentDicom.getManufacturer());
						return manufacturerRepository.save(newManufacturer);
					});
			ManufacturerModel newManufacturerModel = new ManufacturerModel();
			newManufacturerModel.setName(equipmentDicom.getManufacturerModelName());
			newManufacturerModel.setManufacturer(manufacturer);
			Integer modalityTypeId = DatasetModalityType.getIdFromModalityName(equipmentDicom.getModality());
			newManufacturerModel.setDatasetModalityType(DatasetModalityType.getType(modalityTypeId));
			String magneticFieldStrength = equipmentDicom.getMagneticFieldStrength();
			if (magneticFieldStrength == null || magneticFieldStrength.isBlank() || "unknown".equals(magneticFieldStrength)) {
				magneticFieldStrength = "0.0";
			}
			newManufacturerModel.setMagneticField(Double.valueOf(magneticFieldStrength));
			return manufacturerModelRepository.save(newManufacturerModel);
		});
		AcquisitionEquipment equipment = new AcquisitionEquipment();
		equipment.setManufacturerModel(manufacturerModel);
		equipment.setCenter(centerRepository.findById(centerId).orElseThrow());
		equipment.setSerialNumber(equipmentDicom.getDeviceSerialNumber());
		return repository.save(equipment);
	}

	private void matchOrRemoveEquipments(EquipmentDicom equipmentDicom, List<AcquisitionEquipment> equipments) {
		for (Iterator<AcquisitionEquipment> iterator = equipments.iterator(); iterator.hasNext();) {
			AcquisitionEquipment acquisitionEquipment = (AcquisitionEquipment) iterator.next();
			ManufacturerModel manufacturerModel = acquisitionEquipment.getManufacturerModel();
			if (manufacturerModel != null) {
				if (equipmentDicom.getManufacturerModelName().contains(manufacturerModel.getName())) {
					Manufacturer manufacturer = manufacturerModel.getManufacturer();
					if (manufacturer != null) {
						if (equipmentDicom.getManufacturer().contains(manufacturer.getName())) {
							// keep in list, as matching equipment found with high probability
						} else {
							iterator.remove();
						}
					} else {
						iterator.remove();
					}
				} else {
					iterator.remove();
				}
			} else {
				iterator.remove();
			}
		}
	}

}
