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

package org.shanoir.ng.coil.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.repository.CoilRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * center service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class CoilServiceImpl implements CoilService {

	@Autowired
	private CoilRepository repository;

	protected Coil updateValues(final Coil from, final Coil to) {
		to.setCoilType(from.getCoilType());
		to.setName(from.getName());
		to.setNumberOfChannels(from.getNumberOfChannels());
		to.setSerialNumber(from.getSerialNumber());
		to.setCenter(from.getCenter());
		to.setManufacturerModel(from.getManufacturerModel());
		return to;
	}

	public Optional<Coil> findByName(String name) {
		return repository.findByName(name);
	}
	
	public Optional<Coil> findById(final Long id) {
		return repository.findById(id);
	}
	
	@Transactional
	public List<Coil> findAll() {
		List<Coil> coils = repository.findAll();
		// load study center list from database, as findAll does not allow multiple bags in entity graph
		coils.stream().forEach(s -> s.getCenter().getStudyCenterList().size());
		return coils;
	}

	public List<Coil> findByCenterId(Long centerId) {
		return Utils.toList(repository.findByCenterId(centerId));
	}
	
	public Coil create(final Coil entity) {
		Coil savedEntity = repository.save(entity);
		return savedEntity;
	}
	
	public Coil update(final Coil entity) throws EntityNotFoundException {
		final Optional<Coil> entityDbOpt = repository.findById(entity.getId());
		final Coil entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(entity.getClass(), entity.getId()));
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<Coil> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}

}
