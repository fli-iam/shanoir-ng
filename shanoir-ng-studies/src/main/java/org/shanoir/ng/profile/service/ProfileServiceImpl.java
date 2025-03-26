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

package org.shanoir.ng.profile.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.profile.model.Profile;
import org.shanoir.ng.profile.repository.ProfileRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * profile service implementation.
 *
 * @author msimon
 *
 */
@Service
public class ProfileServiceImpl implements ProfileService {

	@Autowired
	private ProfileRepository repository;

	public Optional<Profile> findById(final Long id) {
		return repository.findById(id);
	}
	
	public List<Profile> findAll() {
		return Utils.toList(repository.findAll());
	}
	
	public Profile create(final Profile entity) {
		Profile savedEntity = repository.save(entity);
		return savedEntity;
	}
	
	public Profile update(final Profile entity) throws EntityNotFoundException {
		final Optional<Profile> entityDbOpt = repository.findById(entity.getId());
		final Profile entityDb = entityDbOpt.orElseThrow(
				() -> new EntityNotFoundException(entity.getClass(), entity.getId()));
		updateValues(entity, entityDb);
		return repository.save(entityDb);
	}

	public void deleteById(final Long id) throws EntityNotFoundException  {
		final Optional<Profile> entity = repository.findById(id);
		entity.orElseThrow(() -> new EntityNotFoundException("Cannot find entity with id = " + id));
		repository.deleteById(id);
	}
	
	protected Profile updateValues(Profile from, Profile to) {
		to.setProfileName(from.getProfileName());
		return to;
	}

}
