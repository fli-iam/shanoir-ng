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

package org.shanoir.ng.processing.service;

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * DatasetProcessing service.
 *
 * @author amasson
 *
 */
public interface DatasetProcessingService {

	/**
	 * Find dataset processing by name.
	 *
	 * @param name name.
	 * @return a dataset processing.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<DatasetProcessing> findByComment(String comment);
	
    /**
     * Save an entity.
     *
     * @param entity the entity to create.
     * @return created entity.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and #entity.getId() == null")
    DatasetProcessing create(DatasetProcessing entity);

    /**
     * Update an entity.
     *
     * @param entity the entity to update.
     * @return updated entity.
     * @throws EntityNotFoundException
     * @throws MicroServiceCommunicationException
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    DatasetProcessing update(DatasetProcessing entity) throws EntityNotFoundException;

    /**
     * Find entity by its id.
     *
     * @param id id
     * @return an entity or null.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<DatasetProcessing> findById(Long id);

    /**
     * Get all entities.
     *
     * @return a list of manufacturers.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<DatasetProcessing> findAll();

    /**
     * Delete an entity.
     *
     * @param id the entity id to be deleted.
     * @throws EntityNotFoundException if the entity cannot be found.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    void deleteById(Long id) throws ShanoirException, RestServiceException, SolrServerException, IOException;

    /**
     * Unlink given dataset from all processing
     * Remove processing if linked to no other dataset
     *
     * @param datasetId
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    void removeDatasetFromAllProcessingInput(Long datasetId) throws ShanoirException, RestServiceException, SolrServerException, IOException;

    /**
     * Delete child processing of given processing
     *
     * @param id
     * @throws RestServiceException
     * @throws ShanoirException
     * @throws SolrServerException
     * @throws IOException
     */
    void deleteByParentId(Long id) throws RestServiceException, ShanoirException, SolrServerException, IOException;

    void validateDatasetProcessing(DatasetProcessing processing) throws RestServiceException;
}
