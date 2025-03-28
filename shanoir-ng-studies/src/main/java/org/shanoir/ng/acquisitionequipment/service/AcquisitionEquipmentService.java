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

package org.shanoir.ng.acquisitionequipment.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Acquisition equipment service.
 *
 * @author msimon
 *
 */
public interface AcquisitionEquipmentService {

    /**
     * Find entity by its id.
     *
     * @param id id
     * @return an entity or null.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    Optional<AcquisitionEquipment> findById(Long id);

    /**
     * Get all entities.
     *
     * @return a list of manufacturers.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
    List<AcquisitionEquipment> findAll();

    /**
     * Save an entity.
     *
     * @param entity the entity to create.
     * @return created entity.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null")
    AcquisitionEquipment create(AcquisitionEquipment entity);

    /**
     * Update an entity.
     *
     * @param entity the entity to update.
     * @return updated entity.
     * @throws EntityNotFoundException
     * @throws MicroServiceCommunicationException
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    AcquisitionEquipment update(AcquisitionEquipment entity) throws EntityNotFoundException;

    /**
     * Delete an entity.
     *
     * @param id the entity id to be deleted.
     * @throws EntityNotFoundException if the entity cannot be found.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    void deleteById(Long id) throws EntityNotFoundException;

    List<AcquisitionEquipment> findAllByCenterId(Long centerId);

    List<AcquisitionEquipment> findAllByStudyId(Long studyId);

    List<AcquisitionEquipment> findAllBySerialNumber(String serialNumber);

    List<AcquisitionEquipment> findAcquisitionEquipmentsOrCreateOneByEquipmentDicom(Long centerId, EquipmentDicom equipmentDicom);

}
