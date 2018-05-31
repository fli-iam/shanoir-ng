package org.shanoir.ng.studyuser;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for relations between a study and an user.
 *
 * @author msimon
 */
public interface StudyUserRepository extends CrudRepository<StudyUser, Long> {

	/**
	 * Get a relation between a study and an user.
	 * 
	 * @param studyId
	 *            study id.
	 * @param userId
	 *            user id.
	 * @return relation.
	 */
	StudyUser findByStudyIdAndUserId(Long studyId, Long userId);

}
