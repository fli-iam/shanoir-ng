/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.user.service;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.ExtensionRequestInfo;
import org.shanoir.ng.user.model.User;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User service.
 *
 * @author msimon
 * @author jlouis
 * @author mkain
 */
@Service
public interface UserService {

	/**
	 * Confirm an account request and updates user.
	 * 
	 * @param user the user to update.
	 * @return the updated user.
	 * @throws EntityNotFoundException if this user id doesn't exist in the database.
	 * @throws AccountNotOnDemandException if this account is not currently on demand.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	User confirmAccountRequest(User user) throws EntityNotFoundException, AccountNotOnDemandException;

	/**
	 * Delete a user
	 * 
	 * @param id the user id.
	 * @throws EntityNotFoundException if this user id doesn't exist in the database.
	 */
	@PreAuthorize("hasRole('ADMIN') and !@isMeSecurityService.isMe(#id)")
	void deleteById(Long id) throws EntityNotFoundException;

	/**
	 * Deny an account request.
	 * 
	 * @param userId the user id.
	 * @throws EntityNotFoundException if this user id doesn't exist in the database.
	 * @throws AccountNotOnDemandException if this account is not currently on demand.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	void denyAccountRequest(Long userId) throws EntityNotFoundException, AccountNotOnDemandException;

	/**
	 * Get all the users
	 * 
	 * @return a list of users
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	@PostAuthorize("hasRole('ADMIN') or @userPrivacySecurityService.filterPersonnalData(returnObject)")
	List<User> findAll();

	/**
	 * Find user by its email.
	 *
	 * @param email email.
	 * @return optionally a user.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	Optional<User> findByEmail(String email);

	/**
	 * Find user by its username.
	 *
	 * @param username the username.
	 * @return a user or null.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#username))")
	Optional<User> findByUsername(String username);

	/**
	 * Find users that will soon expire and have not yet received the first notification.
	 * 
	 * @return a list of users.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	List<User> getUsersToReceiveFirstExpirationNotification();

	/**
	 * Find users that will soon expire and have not yet received the second notification.
	 * 
	 * @return a list of users.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	List<User> getUsersToReceiveSecondExpirationNotification();

	/**
	 * Request a date extension for an user.
	 * 
	 * @param userId the user id.
	 * @param requestInfo the request info.
	 * @throws EntityNotFoundException if this user id doesn't exist in the database.
	 */
	void requestExtension(Long userId, ExtensionRequestInfo requestInfo) throws EntityNotFoundException;

	/**
	 * Save a new user.
	 *
	 * @param user the user to create.
	 * @return the created user with its fresh id.
	 * @throws PasswordPolicyException if the given password doesn't meet the security requirements.
	 * @throws SecurityException if the new user could not be register into Keycloak.
	 * In this case the user is not saved in the database either.
	 */
	@PreAuthorize("hasRole('ADMIN') and #user.getId() == null")
	User create(User user) throws PasswordPolicyException, SecurityException;
	
	/**
	 * Create a new account request.
	 *
	 * @param user the user to create.
	 * @return the created user with its fresh id.
	 * @throws PasswordPolicyException if the given password doesn't meet the security requirements.
	 * @throws SecurityException if the new user could not be register into Keycloak.
	 * In this case the user is not saved in the database either.
	 */
	@PreAuthorize("#user.getId() == null && #user.getRole() == null && #user.isAccountRequestDemand() != null && #user.isAccountRequestDemand()")
	User createAccountRequest(User user) throws PasswordPolicyException, SecurityException;

	/**
	 * Search users by their id.
	 * 
	 * @param userIds as list of user ids.
	 * @return list of users with id and username.
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	List<IdName> findByIds(List<Long> userIdList);

	/**
	 * Update a user.
	 *
	 * @param user the user to update.
	 * @return updated user.
	 * @throws EntityNotFoundException if this user id doesn't exist in the database.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#user))")
	User update(User user) throws EntityNotFoundException;

	/**
	 * Update expiration notification for an user.
	 * 
	 * @param user the user to update.
	 * @param firstNotification is it first notification?
	 */
	@PreAuthorize("hasRole('ADMIN')")
	void updateExpirationNotification(User user, boolean firstNotification);

	/**
	 * Update last login date.
	 * 
	 * @param username username.
	 * @throws EntityNotFoundException if this user id doesn't exist in the database.
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	void updateLastLogin(String username) throws EntityNotFoundException;

	/**
	 * Find a user by it's id
	 * 
	 * @param id the id
	 * @return the user
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('USER', 'EXPERT') and @isMeSecurityService.isMe(#id))")
	User findById(Long id);

}
