package org.shanoir.ng.repository;

import org.shanoir.ng.model.User;
import org.springframework.data.repository.CrudRepository;

/**
 * Magic repository for users
 *
 * @author jlouis
 */
public interface UserRepository extends CrudRepository<User, Long>, UserRepositoryCustom {

    /**
     * Find user by its email address
     *
     * @param email
     * @return a user or null
     */
    User findByEmail(String email);

    /**
     * Find user by its username
     *
     * @param id
     * @return a user or null
     */
    User findByUsername(String username);

}
