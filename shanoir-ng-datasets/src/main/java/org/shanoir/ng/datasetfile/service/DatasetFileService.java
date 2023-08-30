package org.shanoir.ng.datasetfile.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.shanoir.ng.datasetfile.DatasetFile;

public interface DatasetFileService {

	/**
	 * Update an entity with the values of another.
	 * 
	 * @param from the entity with the new values.
	 * @param to the instance to update with the new values.
	 * @return the updated instance.
	 */
	abstract DatasetFile updateValues(final DatasetFile from, final DatasetFile to);

	/**
	 * Find entity by its id. 
	 *
	 * @param id id
	 * @return an entity or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<DatasetFile> findById(Long id);

	/**
	 * Get all entities.
	 * 
	 * @return a list of manufacturers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<DatasetFile> findAll();

	/**
	 * Save an entity.
	 *
	 * @param entity the entity to create.
	 * @return created entity.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null")
	DatasetFile create(DatasetFile entity);

	/**
	 * Update an entity.
	 *
	 * @param entity the entity to update.
	 * @return updated entity.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException 
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	DatasetFile update(DatasetFile entity) throws EntityNotFoundException;

	/**
	 * Delete an entity.
	 * 
	 * @param id the entity id to be deleted.
	 * @throws EntityNotFoundException if the entity cannot be found.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteById(Long id) throws EntityNotFoundException;
}