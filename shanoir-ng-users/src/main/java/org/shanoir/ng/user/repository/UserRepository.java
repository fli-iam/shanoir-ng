package org.shanoir.ng.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Magic repository for users
 *
 * @author jlouis
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

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
	 * Find user by its email address
	 *
	 * @param email
	 * @return a user or null
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Find users who have account that will soon expire and have not received
	 * first notification.
	 * 
	 * @param expirationDate
	 *            expiration date to check.
	 * @return list of users.
	 */
	List<User> findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(LocalDate expirationDate);

	/**
	 * Find users who have account that will soon expire and have not received
	 * second notification.
	 * 
	 * @param expirationDate
	 *            expiration date to check.
	 * @return list of users.
	 */
	List<User> findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(LocalDate expirationDate);

	/**
	 * Find users by their id.
	 * 
	 * @param userIdList
	 *            list of user ids.
	 * @return list of users.
	 */
	List<User> findByIdIn(List<Long> userIdList);
	
	/**
	 * Find user by its username
	 *
	 * @param id
	 * @return a user or null
	 */
	Optional<User> findByUsername(String username);

}
