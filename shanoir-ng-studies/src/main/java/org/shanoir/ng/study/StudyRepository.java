package org.shanoir.ng.study;

import java.util.List;

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
}
