package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;

/**
 * Custom repository for subject therapies
 * 
 * @author sloury
 *
 */
public interface SubjectTherapyRepositoryCustom {

	List<SubjectTherapy> findBy(String fieldName, Object value);

	List<SubjectTherapy> findByAnimalSubject(AnimalSubject animalSubject);
}
