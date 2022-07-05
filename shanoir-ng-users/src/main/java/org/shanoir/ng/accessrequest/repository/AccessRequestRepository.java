package org.shanoir.ng.accessrequest.repository;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessRequestRepository  extends CrudRepository<AccessRequest, Long> {

}
