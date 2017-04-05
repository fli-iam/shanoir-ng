package org.shanoir.ng.manufacturermodel;

import java.util.List;

/**
 * Custom repository for manufacturers.
 * 
 * @author msimon
 *
 */
public interface ManufacturerRepositoryCustom {

	/**
	 * Find manufacturers by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of manufacturers.
	 */
	List<Manufacturer> findBy(String fieldName, Object value);

}
