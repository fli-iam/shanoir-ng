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

package org.shanoir.ng.manufacturermodel.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.EntityLinkedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.security.access.prepost.PreAuthorize;


/**
 * Manufacturer model service.
 *
 * @author jlouis
 *
 */
public interface ManufacturerModelService {

    /**
     * Find entity by its id.
     *
     * @param id id
     * @return an entity or null.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<ManufacturerModel> findById(Long id);

    /**
     * Get all entities.
     *
     * @return a list of manufacturers.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<ManufacturerModel> findAll();

    /**
     * Save an entity.
     *
     * @param entity the entity to create.
     * @return created entity.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null")
    ManufacturerModel create(ManufacturerModel entity);

    /**
     * Update an entity.
     *
     * @param entity the entity to update.
     * @return updated entity.
     * @throws EntityNotFoundException
     * @throws MicroServiceCommunicationException
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    ManufacturerModel update(ManufacturerModel entity) throws EntityNotFoundException;

    /**
     * Delete an entity.
     *
     * @param id the entity id to be deleted.
     * @throws EntityNotFoundException if the entity cannot be found.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    void deleteById(Long id) throws EntityNotFoundException, EntityLinkedException;
    /**
     * Find id and name for all manufacturer models.
     *
     * @return list of IdNameDTO.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<IdName> findIdsAndNames();

    /**
     * Find id and name for manufacturer models related to a center.
     *
     * @param centerId: the id of the center
     * @return list of IdNameDTO.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<IdName> findIdsAndNamesForCenter(Long centerId);

}
