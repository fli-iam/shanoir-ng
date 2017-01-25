package org.shanoir.ng.study.repository;

import org.shanoir.ng.study.model.Study;
import org.springframework.data.repository.CrudRepository;

public interface StudyRepository extends CrudRepository<Study, Long>{
	/**
	 * Get all studies
	 */
	Iterable<Study> findAll();

}
