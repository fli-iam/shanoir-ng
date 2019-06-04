package org.shanoir.ng.study.rights;

import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for relations between a study and an user.
 */
public interface StudyUserRightsRepository extends CrudRepository<StudyUser, Long> {

	@Transactional
	void deleteByIdIn(Set<Long> ids); 
	
}
