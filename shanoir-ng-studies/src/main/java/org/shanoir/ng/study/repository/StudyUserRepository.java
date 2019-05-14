package org.shanoir.ng.study.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.shanoir.ng.study.rights.StudyUser;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for relations between a study and an user.
 *
 * @author msimon
 */
public interface StudyUserRepository extends CrudRepository<StudyUser, Long> {

	List<StudyUser> findByUserId(Long userId);
	
	@Transactional
	void deleteByIdIn(Set<Long> ids);
	
}
