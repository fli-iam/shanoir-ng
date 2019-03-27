package org.shanoir.ng.shared.core.service;

import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Service for hardware entities (coil, manufacturer, ...) .
 * 
 * @author jlouis
 *
 * @param <T>
 */
public interface BasicEntityService<T extends AbstractEntity> extends UniqueCheckableService<T> {

	/**
	 * Find an item by a value of a field.
	 * 
	 * @param fieldName searched field name.
	 * @param value value to search.
	 * @return list of items.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<T> findBy(String fieldName, Object value, @SuppressWarnings("rawtypes") Class clazz);
	
	/**
	 * Find an item by a value of a field.
	 * 
	 * @param fieldName searched field name.
	 * @param value value to search.
	 * @return list of items.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<T> findBy(String fieldName, Object value);
	
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
