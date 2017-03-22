package org.shanoir.ng.role;

import java.util.List;
import java.util.Optional;

/**
 * Role service.
 *
 * @author jlouis
 *
 */
public interface RoleService {

    /**
     * Get all the roles
     * @return a list of roles
     */
	List<Role> findAll();
	
	/**
	 * Find role by its name.
	 *
	 * @param name
	 *            name.
	 * @return a role or null/
	 */
	Optional<Role> findByName(String name);

}
