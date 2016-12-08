package org.shanoir.ng.repository;

import org.shanoir.ng.model.User;
import org.springframework.data.repository.CrudRepository;

/**
 * Magic repository for users
 *
 * @author jlouis
 */
public interface UserRepository extends CrudRepository<User, Long> {

	User findByUsername(String username);

	User findByEmail(String email);

}
