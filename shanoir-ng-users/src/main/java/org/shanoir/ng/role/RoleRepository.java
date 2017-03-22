package org.shanoir.ng.role;

import java.util.Optional;

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
