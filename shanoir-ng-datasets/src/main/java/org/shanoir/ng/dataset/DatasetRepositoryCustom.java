package org.shanoir.ng.dataset;

import java.util.List;

/**
 * Custom repository for datasets.
 * 
 * @author msimon
 *
 */
public interface DatasetRepositoryCustom {

	/**
	 * Find datasets by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of datasets.
	 */
	List<Dataset> findBy(String fieldName, Object value);

}
