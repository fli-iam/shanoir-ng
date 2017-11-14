package org.shanoir.ng.coil;

import java.util.List;

/**
 * Custom repository for coils.
 * 
 * @author msimon
 *
 */
public interface CoilRepositoryCustom {

	/**
	 * Find coils by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of coils.
	 */
	List<Coil> findBy(String fieldName, Object value);

}
