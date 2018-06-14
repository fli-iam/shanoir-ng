package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;

/**
 * Custom repository for subjects.
 * 
 * @author sloury
 *
 */
public interface AnimalSubjectRepositoryCustom {

	List<AnimalSubject> findByReference(Reference reference);

	List<AnimalSubject> findBy(String fieldName, Object value);
}
