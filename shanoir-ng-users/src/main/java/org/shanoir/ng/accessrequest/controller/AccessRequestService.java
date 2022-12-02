package org.shanoir.ng.accessrequest.controller;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public interface AccessRequestService extends BasicEntityService<AccessRequest>{

	List<AccessRequest> findByStudyId(List<Long> studiesId);

	@Override
	AccessRequest update(AccessRequest entity);

	AccessRequest createAllowed(AccessRequest entity);

	@Override
	Optional<AccessRequest> findById(Long id);
}
