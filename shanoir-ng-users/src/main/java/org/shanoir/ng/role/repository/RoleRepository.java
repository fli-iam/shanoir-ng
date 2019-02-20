package org.shanoir.ng.role.repository;

import org.shanoir.ng.role.model.Role;
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
	Role findByName(String name);

}
