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

package org.shanoir.ng.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;
import org.keycloak.KeycloakPrincipal;
import org.shanoir.ng.accountrequest.AccountRequestInfoRepository;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.events.UserDeleteEvent;
import org.shanoir.ng.role.RoleRepository;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirAuthenticationException;
import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.shanoir.ng.shared.exception.UsersErrorModelCode;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.PasswordUtils;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * User service implementation.
 * 
 * @author msimon
 * @author mkain
 *
 */
@Service
public class UserServiceImpl implements UserService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private AccountRequestInfoRepository accountRequestInfoRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private KeycloakClient keycloakClient;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    ApplicationEventPublisher publisher;

	@Override
	public User confirmAccountRequest(final Long userId, final User user) throws ShanoirUsersException {
		final User userDb = userRepository.findOne(userId);
		if (userDb == null) {
			LOG.error("User with id " + userId + " not found");
			throw new ShanoirUsersException(UsersErrorModelCode.USER_NOT_FOUND);
		}
		if (!userDb.isAccountRequestDemand() && !userDb.isExtensionRequestDemand()) {
			LOG.error("User with id " + userId + " has no request (account or extension)");
			throw new ShanoirUsersException(UsersErrorModelCode.NO_ACCOUNT_REQUEST);
		}

		// Confirm and update user
		if (userDb.isExtensionRequestDemand()) {
			// Date extension
			userDb.setExtensionRequestInfo(null);
			userDb.setExtensionRequestDemand(false);
			userDb.setFirstExpirationNotificationSent(false);
			userDb.setSecondExpirationNotificationSent(false);
			final User updatedUser = updateUserOnAllSystems(userDb, user);
			// Send emails
			emailService.notifyExtensionRequestAccepted(updatedUser);
			return updatedUser;
		} else {
			// Account creation
			userDb.setAccountRequestDemand(false);
			final User updatedUser = updateUserOnAllSystems(userDb, user);
			// Send email
			emailService.notifyAccountRequestAccepted(updatedUser);
			return updatedUser;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void deleteById(final Long id) throws ShanoirUsersException {
		// Check if connected user tries to delete itself
		final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserContext) {
			// For tests
			if (id.equals(((UserContext) principal).getId())) {
				LOG.error("Forbidden to delete connected user");
				throw new ShanoirUsersException("Forbidden to delete connected user");
			}
		} else {
			final Map<String, Object> otherClaims = ((KeycloakPrincipal) principal).getKeycloakSecurityContext()
					.getToken().getOtherClaims();
			if (otherClaims.containsKey(KeycloakUtil.USER_ID_TOKEN_ATT)
					&& id.equals(Long.valueOf(otherClaims.get(KeycloakUtil.USER_ID_TOKEN_ATT).toString()))) {
				LOG.error("Forbidden to delete connected user");
				throw new ShanoirUsersException("Forbidden to delete connected user");
			}
		}

		final User user = userRepository.findOne(id);
		if (user == null) {
			LOG.error("User with id " + id + " not found");
			throw new ShanoirUsersException(UsersErrorModelCode.USER_NOT_FOUND);
		}
		userRepository.delete(id);
		publisher.publishEvent(new UserDeleteEvent(id));
		keycloakClient.deleteUser(user.getKeycloakId());
	}

	@Override
	public void denyAccountRequest(final Long userId) throws ShanoirUsersException {
		final User user = userRepository.findOne(userId);
		if (user == null) {
			LOG.error("User with id " + userId + " not found");
			throw new ShanoirUsersException(UsersErrorModelCode.USER_NOT_FOUND);
		}
		if (!user.isAccountRequestDemand() && !user.isExtensionRequestDemand()) {
			LOG.error("User with id " + userId + " has no request (account or extension)");
			throw new ShanoirUsersException(UsersErrorModelCode.NO_ACCOUNT_REQUEST);
		}
		if (user.isAccountRequestDemand()) {
			// Remove user
			userRepository.delete(userId);
			keycloakClient.deleteUser(user.getKeycloakId());
			// Send emails
			emailService.notifyAccountRequestDenied(user);
		} else {
			// Deny extension request
			user.setExtensionRequestInfo(null);
			user.setExtensionRequestDemand(false);
			updateUserOnAllSystems(user, null);
			// Send email
			emailService.notifyExtensionRequestDenied(user);
		}
	}

	@Override
	public List<User> findAll() {
		return Utils.toList(userRepository.findAll());
	}

	@Override
	public List<User> findBy(final String fieldName, final Object value) {
		return userRepository.findBy(fieldName, value);
	}

	@Override
	public Optional<User> findByEmail(final String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User findById(final Long id) {
		return userRepository.findOne(id);
	}

	@Override
	public Optional<User> findByUsername(final String username) {
		return userRepository.findByUsername(username);
	}
	
	@Override
	public List<User> getUsersToReceiveFirstExpirationNotification() {
		final DateTime expirationDateTime = new DateTime().withMillisOfDay(0).plusMonths(1);
		return userRepository.findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(expirationDateTime.toDate());
	}

	@Override
	public List<User> getUsersToReceiveSecondExpirationNotification() {
		final DateTime expirationDateTime = new DateTime().withMillisOfDay(0).plusWeeks(1);
		return userRepository.findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(expirationDateTime.toDate());
	}

	@Override
	public void requestExtension(Long userId, ExtensionRequestInfo requestInfo) throws ShanoirUsersException {
		final User user = userRepository.findOne(userId);
		if (user == null) {
			LOG.error("User with id " + userId + " not found");
			throw new ShanoirUsersException(UsersErrorModelCode.USER_NOT_FOUND);
		}
		user.setExtensionRequestDemand(Boolean.TRUE);
		user.setExtensionRequestInfo(requestInfo);
		try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			LOG.error("Error on request extension", e);
			throw new ShanoirUsersException("Error on request extension");
		}
	}

	@Override
	public User save(final User user) throws ShanoirUsersException {
		// Password generation
		final String newPassword = PasswordUtils.generatePassword();
		User savedUser = null;
		try {
			if (user.getAccountRequestInfo() != null) {
				// Save account request info
				accountRequestInfoRepository.save(user.getAccountRequestInfo());
				user.setAccountRequestDemand(true);
				// Set role 'guest'
				user.setRole(roleRepository.findByName("ROLE_GUEST")
						.orElseThrow(() -> new ShanoirUsersException("Error while getting role 'ROLE_GUEST'")));
				user.setExpirationDate(new DateTime().plusYears(1).toDate());
			}
			savedUser = userRepository.save(user);
			if (user.getAccountRequestInfo() != null) {
				// Send email to administrators
				emailService.notifyAdminAccountRequest(savedUser);
			}
		} catch (DataIntegrityViolationException e) {
			LOG.error("Error while creating user", e);
			throw new ShanoirUsersException("Error while creating user");
		}
		final String keycloakUserId = keycloakClient.createUserWithPassword(user, newPassword);
		if (keycloakUserId != null) {
			// Save keycloak id
			savedUser.setKeycloakId(keycloakUserId);
			userRepository.save(savedUser);
		}
		// Send email to user
		emailService.notifyNewUser(savedUser, newPassword);
		return savedUser;
	}

	@Override
	public List<IdNameDTO> findByIds(List<Long> userIdList) {
		final List<User> users = userRepository.findByIdIn(userIdList);
		List<IdNameDTO> result = new ArrayList<>();
		if (users != null) {
			for (User user : users) {
				result.add(new IdNameDTO(user.getId(), user.getUsername()));
			}
		}
		return result;
	}

	@Override
	public User update(final User user) throws ShanoirUsersException {
		final User userDb = userRepository.findOne(user.getId());
		if (userDb == null) {
			LOG.error("User with id " + user.getId() + " not found");
			throw new ShanoirUsersException(UsersErrorModelCode.USER_NOT_FOUND);
		}
		return updateUserOnAllSystems(userDb, user);
	}
	
	@Override
	public void updateExpirationNotification(final User user, final boolean firstNotification) {
		if (firstNotification) {
			user.setFirstExpirationNotificationSent(true);
		} else {
			user.setSecondExpirationNotificationSent(true);
		}
		userRepository.save(user);
	}

	@Override
	public void updateLastLogin(final String username) throws ShanoirUsersException {
		final User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ShanoirUsersException("User with username " + username + " not found"));
		user.setLastLogin(new Date());
		try {
			userRepository.save(user);
		} catch (Exception e) {
			throw new ShanoirAuthenticationException("Error while updating last login date for user " + user.getId(),
					e);
		}
	}

	/*
	 * Update user on all systems: microservice database, Shanoir old and
	 * Keycloak server
	 * 
	 * @param userDb user found in database.
	 * 
	 * @param user user with new values.
	 * 
	 * @return database user with new values.
	 * 
	 * @throws ShanoirUsersException
	 */
	private User updateUserOnAllSystems(final User userDb, final User user) throws ShanoirUsersException {
		if (user != null) {
			updateUserValues(userDb, user);
		}
		try {
			userRepository.save(userDb);
		} catch (Exception e) {
			LOG.error("Error while updating user", e);
			throw new ShanoirUsersException("Error while updating user");
		}
		keycloakClient.updateUser(userDb);
		return userDb;
	}

	/*
	 * Update some values of user to save them in database.
	 * 
	 * @param userDb user found in database.
	 * 
	 * @param user user with new values.
	 * 
	 * @return database user with new values.
	 */
	private User updateUserValues(final User userDb, final User user) {
		userDb.setCanAccessToDicomAssociation(user.isCanAccessToDicomAssociation());
		userDb.setEmail(user.getEmail());
		userDb.setExpirationDate(user.getExpirationDate());
		userDb.setFirstName(user.getFirstName());
		userDb.setLastName(user.getLastName());
		userDb.setRole(user.getRole());
		userDb.setUsername(user.getUsername());
		return userDb;
	}

}
