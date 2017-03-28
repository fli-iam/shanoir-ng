package org.shanoir.ng.study;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface RelStudyUserRepository extends CrudRepository<RelStudyUser, Long>{
	
	Iterable<RelStudyUser> findAll();
	
	 List<RelStudyUser> findAllByUserId(Long userId);

}
