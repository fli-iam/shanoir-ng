package org.shanoir.ng.studyCards;

import java.util.List;

/**
 * Custom repository for templates.
 * 
 * @author msimon
 *
 */
public interface StudyCardRepositoryCustom {

	/**
	 * Find templates by field value.
	 * 
	 * @param fieldName
	 *            searched field name.
	 * @param value
	 *            value.
	 * @return list of templates.
	 */
	List<StudyCard> findBy(String fieldName, Object value);

}
