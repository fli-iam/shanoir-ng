package org.shanoir.ng.study.repository;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.study.model.Study;

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
	
	/**
	 * Find entities by field value.
	 * 
	 * @param fieldName searched field name.
	 * @param value value.
	 * @return list of entities.
	 */
	List<Study> findBy(String fieldName, Object value);

}
