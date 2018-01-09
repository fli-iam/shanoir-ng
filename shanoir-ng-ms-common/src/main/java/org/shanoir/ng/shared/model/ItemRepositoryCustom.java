package org.shanoir.ng.shared.model;

import java.util.List;

/**
 * Custom repository for entities.
 * 
 * @author msimon
 *
 */
public interface ItemRepositoryCustom<T extends AbstractGenericItem> {

	/**
	 * Find entities by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of entities.
	 */
	List<T> findBy(String fieldName, Object value);

}
