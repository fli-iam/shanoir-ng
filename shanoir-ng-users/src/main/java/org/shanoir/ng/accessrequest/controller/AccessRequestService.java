package org.shanoir.ng.accessrequest.controller;

import java.util.List;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.core.service.BasicEntityService;

public interface AccessRequestService extends BasicEntityService<AccessRequest>{

	List<AccessRequest> findByStudyId(List<Long> studiesId);

}
