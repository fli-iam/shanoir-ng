package org.shanoir.ng.repository;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Magic repository for users
 *
 * @author jlouis
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long>, UserRepositoryCustom {

	/**
	 * Find user by its email address
	 *
	 * @param email
	 * @return a user or null
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Find all users for a role.
	 * 
	 * @param roleName
	 *            role name.
	 * @return list of users.
	 */
	@Query("select email from User u where u.role.name='ROLE_ADMIN'")
	List<String> findAdminEmails();
	
	/**
	 * Find user by its username
	 *
	 * @param id
	 * @return a user or null
	 */
	Optional<User> findByUsername(String username);

}
