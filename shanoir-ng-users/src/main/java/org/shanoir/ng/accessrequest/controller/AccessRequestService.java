package org.shanoir.ng.accessrequest.controller;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.core.service.BasicEntityService;

public interface AccessRequestService extends BasicEntityService<AccessRequest>{

	List<AccessRequest> findByUserIdAndStudyId(Long userId, Long studyId);
	
	@Override
	AccessRequest update(AccessRequest entity);

	AccessRequest createAllowed(AccessRequest entity);

	@Override
	Optional<AccessRequest> findById(Long id);

	List<AccessRequest> findByStudyIdAndStatus(List<Long> studiesId, int status);

	List<AccessRequest> findByUserId(Long userId);
}
