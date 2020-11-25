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

import java.util.Optional;

import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Coil service.
 *
 * @author msimon
 *
 */
public interface CoilService extends BasicEntityService<Coil> {

	/**
	 * Find coil by name.
	 *
	 * @param name name.
	 * @return a coil.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<Coil> findByName(String name);

}
