package org.shanoir.ng.center;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for centers.
 *
 * @author msimon
 */
public interface CenterRepository extends CrudRepository<Center, Long>, CenterRepositoryCustom {

	/**
	 * Find center by name.
	 *
	 * @param name
	 *            name.
	 * @return a center.
	 */
	Optional<Center> findByName(String name);
	
	String findNameById(Long id);

}
