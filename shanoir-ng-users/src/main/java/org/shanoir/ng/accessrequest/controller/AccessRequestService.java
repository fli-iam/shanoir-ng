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
