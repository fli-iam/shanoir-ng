package org.shanoir.ng.center;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface CenterRepositoryCustom {

	/**
	 * Find centers by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of centers.
	 */
	List<Center> findBy(String fieldName, Object value);

	/**
	 * Find id and name for all centers.
	 * 
	 * @return list of centers.
	 */
	List<IdNameDTO> findIdsAndNames();

}
