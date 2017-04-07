package org.shanoir.ng.subject;

import java.util.List;

/**
 * Custom repository for templates.
 * 
 * @author msimon
 *
 */
public interface SubjecStudytRepositoryCustom {

	/**
	 * Find templates by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of templates.
	 */
	List<SubjectStudy> findBy(String fieldName, Object value);

}
