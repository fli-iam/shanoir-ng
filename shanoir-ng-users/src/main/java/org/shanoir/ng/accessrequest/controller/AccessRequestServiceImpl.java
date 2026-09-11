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
import org.shanoir.ng.accessrequest.repository.AccessRequestRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessRequestServiceImpl implements AccessRequestService {

    @Autowired
    private AccessRequestRepository accessRequestRepository;

    @Override
    public Optional<AccessRequest> findById(Long id) {
        return this.accessRequestRepository.findById(id);
    }

    public List<AccessRequest> findAll() {
        return Utils.toList(this.accessRequestRepository.findAll());
    }

    public AccessRequest create(AccessRequest entity) {
        return this.accessRequestRepository.save(entity);
    }

    @Override
    public AccessRequest createAllowed(AccessRequest entity) {
        return this.accessRequestRepository.save(entity);
    }

    @Override
    public AccessRequest update(AccessRequest entity) {
        return this.accessRequestRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) throws EntityNotFoundException {
        this.accessRequestRepository.deleteById(id);
    }

    @Override
    public List<AccessRequest> findByStudyIdAndStatus(List<Long> studiesId, int status) {
        return this.accessRequestRepository.findByStudyIdInAndStatus(studiesId, status);
    }

    @Override
    public List<AccessRequest> findByUserIdAndStudyId(Long userId, Long studyId) {
        return this.accessRequestRepository.findByUserIdAndStudyId(userId, studyId);
    }

    @Override
    public List<AccessRequest> findByUserId(Long userId) {
        return this.accessRequestRepository.findByUserId(userId);
    }

    @Override
    public void requestExtension(Long studyId, Long userId, java.time.LocalDate extensionDate) {
        List<AccessRequest> accessRequests = this.accessRequestRepository.findByUserIdAndStudyId(userId, studyId);
        if (!accessRequests.isEmpty()) {
            AccessRequest accessRequest = accessRequests.get(0);
            accessRequest.setExpirationDate(extensionDate);
            accessRequest.setStatus(AccessRequest.ON_EXTENSION_DEMAND);
            this.accessRequestRepository.save(accessRequest);
        } else {
            AccessRequest accessRequest = new AccessRequest();
            accessRequest.setStudyId(studyId);
            User user = new User();
            user.setId(userId);
            accessRequest.setUser(user);
            accessRequest.setExpirationDate(extensionDate);
            accessRequest.setStatus(AccessRequest.ON_EXTENSION_DEMAND);
            accessRequest.setMotivation("This is an extension request, please check the asked expiration date.");
            this.accessRequestRepository.save(accessRequest);
        }
    }

}
