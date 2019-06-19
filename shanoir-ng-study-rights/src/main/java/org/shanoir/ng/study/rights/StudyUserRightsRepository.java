package org.shanoir.ng.study.rights;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for relations between a study and an user.
 */
public interface StudyUserRightsRepository extends CrudRepository<StudyUser, Long> {

	@Transactional
	void deleteByIdIn(Set<Long> ids);

	StudyUser findByUserIdAndStudyId(Long userId, Long studyId);

	Iterable<StudyUser> findByUserIdAndStudyIdIn(Long userId, Set<Long> studyIds);

	Iterable<StudyUser> findByUserId(Long userId);

	@Query("select su.studyId from StudyUser su where su.userId = :userId and :right in elements(su.studyUserRights)")
	List<Long> findDistinctStudyIdByUserId(@Param("userId") Long userId, @Param("right") int right);
}
