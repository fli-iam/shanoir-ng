package org.shanoir.ng.subject;

import java.util.List;

/**
 * Custom repository for templates.
 * 
 * @author msimon
 *
 */
public interface SubjectRepositoryCustom {

	/**
	 * Find Subjects by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of Subjects.
	 */
	List<Subject> findBy(String fieldName, Object value);

}
