package org.shanoir.ng.repository;

import java.util.List;

import org.shanoir.ng.model.Center;

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

}
