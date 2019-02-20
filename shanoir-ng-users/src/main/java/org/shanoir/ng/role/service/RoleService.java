package org.shanoir.ng.role.service;

import java.util.List;

import org.shanoir.ng.role.model.Role;

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
	Role findByName(String name);

}
