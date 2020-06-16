/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.study.repository;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends CrudRepository<Study, Long> {

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
	 * Get ids and names of all studies.
	 * 
	 * @return IdName list.
	 */
	@Query("select new org.shanoir.ng.shared.core.model.IdName(s.id, s.name) from Study s")
	List<IdName> findIdsAndNames();
	
	/**
	 * Find id and name for all studies in which user has a defined role
	 * 
	 * @param userId
	 * @param studyUserRightId
	 * @return
	 */
	List<IdName> findIdsAndNamesByStudyUserList_UserIdAndStudyUserList_StudyUserRights_OrderByNameAsc(Long userId, Integer studyUserRightId);

}
