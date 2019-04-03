package org.shanoir.ng.shared.validation;

import java.util.List;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * Custom repository for entities.
 * 
 * @author msimon
 *
 */
public interface FindByRepository<T extends AbstractEntity> {

	/**
	 * Find entities by field value.
	 * 
	 * @param fieldName searched field name.
	 * @param value value.
	 * @return list of entities.
	 */
	List<T> findBy(String fieldName, Object value, @SuppressWarnings("rawtypes") Class clazz);

}
