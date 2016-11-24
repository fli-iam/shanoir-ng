package org.shanoir.ng.service.repository;

import org.shanoir.ng.model.Role;
import org.springframework.data.repository.CrudRepository;

/**
 * Magic repository for roles
 *
 * @author jlouis
 */
public interface RoleRepository extends CrudRepository<Role, Long> {

}
