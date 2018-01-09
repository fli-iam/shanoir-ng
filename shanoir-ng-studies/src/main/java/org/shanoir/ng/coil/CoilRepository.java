package org.shanoir.ng.coil;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for coils.
 *
 * @author msimon
 */
public interface CoilRepository extends CrudRepository<Coil, Long> {

	/**
	 * Find coil by name.
	 *
	 * @param name
	 *            name.
	 * @return a coil.
	 */
	Optional<Coil> findByName(String name);

}
