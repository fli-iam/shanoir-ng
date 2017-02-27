package org.shanoir.ng.repository;

import java.util.Optional;

import org.shanoir.ng.model.Center;
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
