package org.shanoir.ng.service;

import java.util.List;

import org.shanoir.ng.model.Role;

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
	
}
