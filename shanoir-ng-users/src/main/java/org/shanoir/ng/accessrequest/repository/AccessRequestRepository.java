package org.shanoir.ng.accessrequest.repository;

import java.util.List;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessRequestRepository  extends CrudRepository<AccessRequest, Long> {

    List<AccessRequest> findByStudyIdInAndStatus(List<Long> studiesId, int status);

    List<AccessRequest> findByUserIdAndStudyId(Long userId, Long studyId);

    List<AccessRequest> findByUserId(Long userId);

}
