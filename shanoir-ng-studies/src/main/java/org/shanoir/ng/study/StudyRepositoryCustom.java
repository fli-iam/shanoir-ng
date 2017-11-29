package org.shanoir.ng.study;

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
	 * Find studies by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of studies.
	 */
	List<Study> findBy(String fieldName, Object value);
	
	/**
	 * Find id and name for all studies.
	 * 
	 * @return list of studies.
	 */
	List<IdNameDTO> findIdsAndNames();

}
