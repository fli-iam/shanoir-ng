package org.shanoir.ng.accountrequest.repository;

import org.shanoir.ng.accountrequest.model.AccountRequestInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for account request info.
 * 
 * @author msimon
 *
 */
@Repository
public interface AccountRequestInfoRepository extends CrudRepository<AccountRequestInfo, Long> {

}
