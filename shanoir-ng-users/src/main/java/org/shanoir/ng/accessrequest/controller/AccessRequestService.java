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

package org.shanoir.ng.accessrequest.controller;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

public interface AccessRequestService {

    List<AccessRequest> findByUserIdAndStudyId(Long userId, Long studyId);

    AccessRequest update(AccessRequest entity);

    AccessRequest createAllowed(AccessRequest entity);

    Optional<AccessRequest> findById(Long id);

    List<AccessRequest> findByStudyIdAndStatus(List<Long> studiesId, int status);

    List<AccessRequest> findByUserId(Long userId);

    @PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
    void deleteById(Long id) throws EntityNotFoundException;

}
