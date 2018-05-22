package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.springframework.data.repository.CrudRepository;

public interface SubjectPathologyRepository
		extends CrudRepository<SubjectPathology, Long>, SubjectPathologyRepositoryCustom {

	List<SubjectPathology> findByAnimalSubject(AnimalSubject animalSubject);
}
