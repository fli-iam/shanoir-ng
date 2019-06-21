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

package org.shanoir.ng.center.service;

import java.util.List;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Center service.
 *
 * @author msimon
 * @author jlouis
 */
public interface CenterService extends BasicEntityService<Center> {

	/**
	 * Delete a center.
	 * 
	 * @param id center id.
	 * @throws EntityNotFoundException when the id could not be found in the database.
	 * @throws UndeletableDependenciesException if the center has dependencies that cannot be deleted.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteByIdCheckDependencies(Long id) throws EntityNotFoundException, UndeletableDependenciesException;

	/**
	 * Find center by name.
	 *
	 * @param name center name.
	 * @return a center.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Center findByName(String name);


	/**
	 * Find id and name for all centers.
	 * 
	 * @return list of centers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdName> findIdsAndNames();

	/**
	 * Find id and name for all centers.
	 * 
	 * @param studyId
	 * @return list of centers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdName> findIdsAndNames(Long studyId);

}
