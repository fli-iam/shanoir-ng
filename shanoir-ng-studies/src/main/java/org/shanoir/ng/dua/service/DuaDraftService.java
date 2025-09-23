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
import org.shanoir.ng.shared.exception.EntityFoundException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public interface DuaDraftService {

	Optional<DuaDraft> findById(final String id);

	DuaDraft create(final DuaDraft dua) throws EntityFoundException;

	DuaDraft update(final DuaDraft dua) throws EntityNotFoundException;
}
