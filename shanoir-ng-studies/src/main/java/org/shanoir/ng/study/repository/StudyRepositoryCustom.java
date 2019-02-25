package org.shanoir.ng.study.repository;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.model.ItemRepositoryCustom;
import org.shanoir.ng.study.model.Study;

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

}
