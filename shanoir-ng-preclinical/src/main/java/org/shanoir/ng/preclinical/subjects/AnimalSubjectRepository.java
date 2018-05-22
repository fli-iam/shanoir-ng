package org.shanoir.ng.preclinical.subjects;

import org.springframework.data.repository.CrudRepository;

public interface AnimalSubjectRepository extends CrudRepository<AnimalSubject, Long>, AnimalSubjectRepositoryCustom {

}
