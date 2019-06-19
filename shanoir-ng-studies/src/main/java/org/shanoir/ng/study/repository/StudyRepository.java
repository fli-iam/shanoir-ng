package org.shanoir.ng.study.repository;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends CrudRepository<Study, Long>, StudyRepositoryCustom {

	/**
	 * Get all studies
	 * 
	 * @return list of studies.
	 */
	List<Study> findAll();

	/**
	 * Get studies linked to an user.
	 * 
	 * @param userId
	 *            user id.
	 * @return list of studies.
	 */
	List<Study> findByStudyUserList_UserIdOrderByNameAsc(Long userId);
	
	
	/**
	 * Get studies linked to an user.
	 * 
	 * @param userId
	 *            user id.
	 * @return list of studies.
	 */
	List<Study> findByStudyUserList_UserIdAndStudyUserList_StudyUserRights_OrderByNameAsc(Long userId, Integer studyUseRightId);
	
	
	/**
	 * Find id and name for all studies in which user has a defined role
	 * 
	 * @param userId
	 * @param studyUserRightId
	 * @return
	 */
	List<IdName> findIdsAndNamesByStudyUserList_UserIdAndStudyUserList_StudyUserRights_OrderByNameAsc(Long userId, Integer studyUserRightId);

}
