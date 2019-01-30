package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
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
	List<Study> findByStudyUserList_UserIdAndStudyUserList_StudyUserTypeEqualOrderByNameAsc(Long userId, Integer studyUserTypeId);
	
	
	/**
	 * Find id and name for all studies in which user has a defined role
	 * 
	 * @param userId
	 * @param studyUserTypeId
	 * @return
	 */
	List<IdNameDTO> findIdsAndNamesByStudyUserList_UserIdAndStudyUserList_StudyUserTypeEqualOrderByNameAsc(Long userId, Integer studyUserTypeId);

}
