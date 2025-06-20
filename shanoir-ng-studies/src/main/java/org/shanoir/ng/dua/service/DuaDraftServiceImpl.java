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

package org.shanoir.ng.dua.service;

import java.util.Optional;

import org.shanoir.ng.dua.model.DuaDraft;
import org.shanoir.ng.dua.repository.DuaDraftRepository;
import org.shanoir.ng.shared.exception.EntityFoundException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DuaDraftServiceImpl implements DuaDraftService {

	@Autowired
	private DuaDraftRepository duaDraftRepository;

	
	@Override
	public Optional<DuaDraft> findById(final String id) {
		return duaDraftRepository.findById(id);
	}

	@Override
	public DuaDraft create(final DuaDraft dua) throws EntityFoundException {
		if (duaDraftRepository.existsById(dua.getId())) {
			throw new EntityFoundException("dua draft with this id already exists");
		} else {
			return duaDraftRepository.save(dua);
		}
	}

	@Override
	public DuaDraft update(final DuaDraft dua) throws EntityNotFoundException {
		if (duaDraftRepository.existsById(dua.getId())) {
			return duaDraftRepository.save(dua);
		} else {
			throw new EntityNotFoundException("dua draft with this id doesn't exist");
		}
	}
}
