package org.shanoir.ng.user.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ForbiddenException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;
import org.shanoir.ng.user.model.ExtensionRequestInfo;
import org.shanoir.ng.user.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User service.
 *
 * @author msimon
 * @author jlouis
 * @author mkain
 *
 */
@Service
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
	@PreAuthorize("hasRole('ADMIN')")
	User confirmAccountRequest(User user) throws EntityNotFoundException, AccountNotOnDemandException;

	/**
	 * Delete a user
	 * 
	 * @param id
	 * @throws ForbiddenException 
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasRole('ADMIN') and !@isMeSecurityService.isMe(#id)")
	void deleteById(Long id) throws EntityNotFoundException, ForbiddenException;

	/**
	 * Denies an account request.
	 * 
	 * @param userId
	 *            user id.
	 * @throws AccountNotOnDemandException 
	 * @throws ShanoirUsersException
	 */
	@PreAuthorize("hasRole('ADMIN')")
	void denyAccountRequest(Long userId) throws EntityNotFoundException, AccountNotOnDemandException;

	/**
	 * Get all the users
	 * 
	 * @return a list of users
	 */
	@PreAuthorize("hasRole('ADMIN')")
	List<User> findAll();

	/**
	 * Find user by its email.
	 *
	 * @param email
	 *            email.
	 * @return a user or null.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	Optional<User> findByEmail(String email);

	/**
	 * Find user by its id.
	 *
	 * @param id
	 *            user id.
	 * @return a user or null.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#id))")
	User findById(Long id);

	/**
	 * Find user by its username.
	 *
	 * @param username
	 *            user name.
	 * @return a user or null.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#username))")
	Optional<User> findByUsername(String username);

	/**
	 * Find users who have account that will soon expire and have not received
	 * first notification.
	 * 
	 * @return list of users.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	List<User> getUsersToReceiveFirstExpirationNotification();

	/**
	 * Find users who have account that will soon expire and have not received
	 * second notification.
	 * 
	 * @return list of users.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	List<User> getUsersToReceiveSecondExpirationNotification();

	/**
	 * Request a date extension for an user.
	 * 
	 * @param userId
	 *            user id.
	 * @param requestInfo
	 *            request info.
	 * @throws ShanoirUsersException
	 */
	void requestExtension(Long userId, ExtensionRequestInfo requestInfo) throws EntityNotFoundException;

	/**
	 * Save a new user.
	 *
	 * @param user
	 *            user to create.
	 * @return created user.
	 * @throws PasswordPolicyException 
	 * @throws ShanoirUsersException
	 */
	User save(User user) throws PasswordPolicyException;

	/**
	 * Search users by their id.
	 * 
	 * @param userIds
	 *            list of user ids.
	 * @return list of users with id and username.
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	List<IdNameDTO> findByIds(List<Long> userIdList);

	/**
	 * Update a user.
	 *
	 * @param user
	 *            user to update.
	 * @return updated user.
	 * @throws ShanoirUsersException
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#user))")
	User update(User user) throws EntityNotFoundException;

	/**
	 * Update expiration notification for an user.
	 * 
	 * @param user
	 *            user to update.
	 * @param firstNotification
	 *            is it first notification?
	 */
	@PreAuthorize("hasRole('ADMIN')")
	void updateExpirationNotification(User user, boolean firstNotification);

	/**
	 * Update last login date.
	 * 
	 * @param username
	 *            username.
	 * @throws EntityNotFoundException 
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	void updateLastLogin(String username) throws EntityNotFoundException;

}
