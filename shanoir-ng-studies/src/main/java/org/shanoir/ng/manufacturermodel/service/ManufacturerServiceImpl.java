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

import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.stereotype.Service;

/**
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerServiceImpl implements ManufacturerService {

	private ManufacturerRepository repository;

	protected Manufacturer updateValues(Manufacturer from, Manufacturer to) {
		return to;
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

	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<Manufacturer> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}

}
