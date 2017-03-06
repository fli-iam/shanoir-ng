package org.shanoir.ng.repository;

import java.util.Optional;

import org.shanoir.ng.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Magic repository for roles
 *
 * @author jlouis
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, Long>, RoleRepositoryCustom {

	/**
	 * Find role by its name.
	 *
	 * @param name
	 *            name.
	 * @return a role or null/
	 */
	Optional<Role> findByName(String name);

}
