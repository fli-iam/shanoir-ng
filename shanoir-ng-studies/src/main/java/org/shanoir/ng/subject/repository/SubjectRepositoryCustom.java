package org.shanoir.ng.subject.repository;

import java.util.List;

import org.shanoir.ng.subject.model.Subject;

/**
 * Custom repository for subjects.
 * 
 * @author msimon
 *
 */
public interface SubjectRepositoryCustom {
	
	/**
	 * Find subject by Id with subject study info (since it is a Lazy Loading).
	 * 
	 * @Param Long id;
	 * 
	 * @return Subject.
	 */
	Subject findSubjectWithSubjectStudyById(Long id);
	
	/**
	 * Find entities by field value.
	 * 
	 * @param fieldName searched field name.
	 * @param value value.
	 * @return list of entities.
	 */
	List<Subject> findBy(String fieldName, Object value);


}
