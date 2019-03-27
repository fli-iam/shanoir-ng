package org.shanoir.ng.study.repository;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
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
	List<Study> findByStudyUserList_UserIdAndStudyUserList_StudyUserRightOrderByNameAsc(Long userId, Integer studyUseRightId);
	
	
	/**
	 * Find id and name for all studies in which user has a defined role
	 * 
	 * @param userId
	 * @param studyUserRightId
	 * @return
	 */
	List<IdNameDTO> findIdsAndNamesByStudyUserList_UserIdAndStudyUserList_StudyUserRightOrderByNameAsc(Long userId, Integer studyUserRightId);

}
