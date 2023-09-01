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

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerModelServiceImpl implements ManufacturerModelService {
	
	@Autowired
	private ManufacturerModelRepository repository;

	public Optional<ManufacturerModel> findById(final Long id) {
		return repository.findById(id);
	}
	
	@Override
	public List<ManufacturerModel> findAll() {
		return Utils.toList(repository.findAll());
	}
	
	@Override
	public ManufacturerModel create(final ManufacturerModel entity) {
		ManufacturerModel savedEntity = repository.save(entity);
		return savedEntity;
	}
	
	@Override
	public ManufacturerModel update(final ManufacturerModel entity) throws EntityNotFoundException {
		final Optional<ManufacturerModel> entityDbOpt = repository.findById(entity.getId());
		final ManufacturerModel entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(entity.getClass(), entity.getId()));
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<ManufacturerModel> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}
	
	@Override
	public List<IdName> findIdsAndNames() {
		return repository.findIdsAndNames();
	}

	@Override
	public List<IdName> findIdsAndNamesForCenter(Long centerId) {
		return repository.findIdsAndNames();
	}

	protected ManufacturerModel updateValues(ManufacturerModel from, ManufacturerModel to) {
		to.setDatasetModalityType(from.getDatasetModalityType());
		to.setMagneticField(from.getMagneticField());
		to.setManufacturer(from.getManufacturer());
		to.setName(from.getName());
		return to;
	}

}
