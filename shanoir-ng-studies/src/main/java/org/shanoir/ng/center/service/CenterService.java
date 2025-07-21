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
import java.util.Optional;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Center service.
 *
 * @author msimon
 * @author jlouis
 */
public interface CenterService {

	/**
	 * Find entity by its id. 
	 *
	 * @param id id
	 * @return an entity or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<Center> findById(Long id);
	
	/**
	 * Get all entities.
	 * 
	 * @return a list of manufacturers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<Center> findAll();
	
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
	Optional<Center> findByName(String name);

	/**
	 * Find id and name for all centers.
	 * 
	 * @param studyId
	 * @return list of centers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<Center> findByStudy(Long studyId);

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
	
	/**
	 * Save an entity.
	 *
	 * @param entity the entity to create.
	 * @return created entity.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and #center.getId() == null")
	Center create(Center center);
	
	/**
	 * Update an entity.
	 *
	 * @param entity the entity to update.
	 * @return updated entity.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException 
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	Center update(Center center) throws EntityNotFoundException;

	/**
	 * Delete an entity.
	 * 
	 * @param id the entity id to be deleted.
	 * @throws EntityNotFoundException if the entity cannot be found.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteById(Long id) throws EntityNotFoundException;

}
