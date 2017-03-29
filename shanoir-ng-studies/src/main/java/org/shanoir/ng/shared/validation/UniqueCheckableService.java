package org.shanoir.ng.shared.validation;

import java.util.List;

import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Service for unicity validator.
 * 
 * @author msimon
 *
 * @param <T>
 */
public interface UniqueCheckableService<T extends AbstractGenericItem> {

	/**
	 * Find an item by a value of a field.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value to search.
	 * @return list of items.
	 */
	List<T> findBy(String fieldName, Object value);

}
