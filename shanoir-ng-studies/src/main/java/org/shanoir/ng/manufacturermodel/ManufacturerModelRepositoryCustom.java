package org.shanoir.ng.manufacturermodel;

import java.util.List;

/**
 * Custom repository for manufacturer models.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelRepositoryCustom {

	/**
	 * Find manufacturer models by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of manufacturer models.
	 */
	List<ManufacturerModel> findBy(String fieldName, Object value);

}
