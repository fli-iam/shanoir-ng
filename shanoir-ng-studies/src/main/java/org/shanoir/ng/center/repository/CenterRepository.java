package org.shanoir.ng.center.repository;

import org.shanoir.ng.center.model.Center;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for centers.
 *
 * @author msimon
 */
public interface CenterRepository extends CrudRepository<Center, Long>, CenterRepositoryCustom {


	Center findByName(String name);
	
	String findNameById(Long id);


}
