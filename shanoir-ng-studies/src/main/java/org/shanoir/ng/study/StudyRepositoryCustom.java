package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.model.ItemRepositoryCustom;

/**
 * Custom repository for studies.
 * 
 * @author msimon
 *
 */
public interface StudyRepositoryCustom extends ItemRepositoryCustom<Study> {

	/**
	 * Find id and name for all studies.
	 * 
	 * @return list of studies.
	 */
	List<IdNameDTO> findIdsAndNames();

	/**
	 * Get list of study name and id for a given user
	 * 
	 * @param userId
	 * @return
	 */
	public List<IdNameDTO> findIdsAndNamesByUserId(Long userId);

}
