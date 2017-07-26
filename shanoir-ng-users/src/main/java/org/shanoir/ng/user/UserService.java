package org.shanoir.ng.user;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * User service.
 *
 * @author msimon
 * @author jlouis
 *
 */
public interface UserService extends UniqueCheckableService<User> {

	/**
	 * Confirms an account request and updates user.
	 * 
	 * @param userId
	 *            user id.
	 * @param user
	 *            updated user.
	 * @return updated user.
	 * @throws ShanoirUsersException
	 */
	User confirmAccountRequest(Long userId, User user) throws ShanoirUsersException;

	/**
	 * Delete a user
	 * 
	 * @param id
	 * @throws ShanoirUsersException
	 */
	void deleteById(Long id) throws ShanoirUsersException;

	/**
	 * Denies an account request.
	 * 
	 * @param userId
	 *            user id.
	 * @throws ShanoirUsersException
	 */
	void denyAccountRequest(Long userId) throws ShanoirUsersException;

	/**
	 * Get all the users
	 * 
	 * @return a list of users
	 */
	List<User> findAll();

	/**
	 * Find user by its email.
	 *
	 * @param email
	 *            email.
	 * @return a user or null.
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Find user by its id.
	 *
	 * @param id
	 *            user id.
	 * @return a user or null.
	 */
	User findById(Long id);

	/**
	 * Find user by its username.
	 *
	 * @param username
	 *            user name.
	 * @return a user or null.
	 */
	Optional<User> findByUsername(String username);

	/**
	 * Request a date extension for an user.
	 * 
	 * @param userId
	 *            user id.
	 * @param requestInfo
	 *            request info.
	 * @throws ShanoirUsersException
	 */
	void requestExtension(Long userId, ExtensionRequestInfo requestInfo) throws ShanoirUsersException;

	/**
	 * Save a user.
	 *
	 * @param user
	 *            user to create.
	 * @return created user.
	 * @throws ShanoirUsersException
	 */
	User save(User user) throws ShanoirUsersException;

	/**
	 * Update a user.
	 *
	 * @param user
	 *            user to update.
	 * @return updated user.
	 * @throws ShanoirUsersException
	 */
	User update(User user) throws ShanoirUsersException;

	/**
	 * Update a user from the old Shanoir
	 * 
	 * @param user
	 * @throws ShanoirUsersException
	 */
	void updateFromShanoirOld(User user) throws ShanoirUsersException;

	/**
	 * Update last login date.
	 * 
	 * @param username
	 *            username.
	 * @throws ShanoirUsersException
	 */
	void updateLastLogin(String username) throws ShanoirUsersException;

}
