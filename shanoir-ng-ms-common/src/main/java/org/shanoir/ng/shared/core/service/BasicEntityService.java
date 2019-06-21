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

package org.shanoir.ng.shared.core.service;

import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Service for hardware entities (coil, manufacturer, ...) .
 * 
 * @author jlouis
 *
 * @param <T>
 */
public interface BasicEntityService<T extends AbstractEntity> {

	/**
	 * Find entity by its id. 
	 *
	 * @param id id
	 * @return an entity or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	T findById(Long id);
	
	/**
	 * Get all entities.
	 * 
	 * @return a list of manufacturers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<T> findAll();

	/**
	 * Save an entity.
	 *
	 * @param entity the entity to create.
	 * @return created entity.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null")
	T create(T entity);

	/**
	 * Update an entity.
	 *
	 * @param entity the entity to update.
	 * @return updated entity.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	T update(T entity) throws EntityNotFoundException;
	
	/**
	 * Delete an entity.
	 * 
	 * @param id the entity id to be deleted.
	 * @throws EntityNotFoundException if the entity cannot be found.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteById(Long id) throws EntityNotFoundException;

}
