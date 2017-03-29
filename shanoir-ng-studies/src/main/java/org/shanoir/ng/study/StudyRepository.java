package org.shanoir.ng.study;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends CrudRepository<Study, Long>{

	/**
	 * Get all studies
	 */
	List<Study> findAll();	


}
