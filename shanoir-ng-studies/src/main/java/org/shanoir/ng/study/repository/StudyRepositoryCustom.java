package org.shanoir.ng.study.repository;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Custom repository for studies.
 * 
 * @author msimon
 *
 */
public interface StudyRepositoryCustom {

	/**
	 * Find id and name for all studies.
	 * 
	 * @return list of studies.
	 */
	List<IdNameDTO> findIdsAndNames();

}
