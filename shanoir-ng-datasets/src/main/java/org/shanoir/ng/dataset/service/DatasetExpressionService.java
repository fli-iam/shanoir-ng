package org.shanoir.ng.dataset.service;

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.shanoir.ng.shared.exception.EntityNotFoundException;

import org.shanoir.ng.dataset.model.DatasetExpression;

public interface DatasetExpressionService {

	DatasetExpression updateValues(DatasetExpression from, DatasetExpression to);
	
	/**
	 * Find entity by its id. 
	 *
	 * @param id id
	 * @return an entity or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<DatasetExpression> findById(Long id);

	/**
	 * Get all entities.
	 * 
	 * @return a list of manufacturers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<DatasetExpression> findAll();

	/**
	 * Save an entity.
	 *
	 * @param entity the entity to create.
	 * @return created entity.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null")
	DatasetExpression create(DatasetExpression entity);

	/**
	 * Update an entity.
	 *
	 * @param entity the entity to update.
	 * @return updated entity.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException 
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	DatasetExpression update(DatasetExpression entity) throws EntityNotFoundException;

	/**
	 * Delete an entity.
	 * 
	 * @param id the entity id to be deleted.
	 * @throws EntityNotFoundException if the entity cannot be found.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteById(Long id) throws EntityNotFoundException;
	
}