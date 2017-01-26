package org.shanoir.ng.repository;

import java.util.Optional;

import org.shanoir.ng.model.Role;
import org.springframework.data.repository.CrudRepository;

/**
 * Magic repository for roles
 *
 * @author jlouis
 */
public interface RoleRepository extends CrudRepository<Role, Long> {

	/**
	 * Find role by its name.
	 *
	 * @param name
	 *            name.
	 * @return a role or null/
	 */
	Optional<Role> findByName(String name);

}
