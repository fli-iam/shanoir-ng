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

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.repository.AcquisitionEquipmentRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
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
	
	public AcquisitionEquipment create(final AcquisitionEquipment entity) {
		AcquisitionEquipment savedEntity = repository.save(entity);
		return savedEntity;
	}
	
	public AcquisitionEquipment update(final AcquisitionEquipment entity) throws EntityNotFoundException {
		final Optional<AcquisitionEquipment> entityDbOpt = repository.findById(entity.getId());
		final AcquisitionEquipment entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(entity.getClass(), entity.getId()));
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<AcquisitionEquipment> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}
	
}
