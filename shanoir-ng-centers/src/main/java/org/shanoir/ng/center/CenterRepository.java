package org.shanoir.ng.center;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for centers.
 *
 * @author msimon
 */
public interface CenterRepository extends CrudRepository<Center, Long>, CenterRepositoryCustom {

//	/**
//	 * Find center by data.
//	 *
//	 * @param data
//	 *            data.
//	 * @return a center.
//	 */
//	Optional<Center> findByData(String data);

}
