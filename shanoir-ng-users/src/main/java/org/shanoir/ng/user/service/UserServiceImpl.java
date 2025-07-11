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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.accessrequest.controller.AccessRequestService;
import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.accessrequest.repository.AccessRequestRepository;
import org.shanoir.ng.accountrequest.repository.AccountRequestInfoRepository;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.events.UserDeleteEvent;
import org.shanoir.ng.extensionrequest.model.ExtensionRequestInfo;
import org.shanoir.ng.role.repository.RoleRepository;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.PasswordUtils;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User service implementation.
 * 
 * @author msimon
 * @author mkain
 *
 */
@Component
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
	private AccessRequestRepository accessRequestRepository;
	
	@Autowired
    ApplicationEventPublisher publisher;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ShanoirEventService eventService;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	AccessRequestService accessRequestService;
	
	@Override
	public User confirmAccountRequest(final User user) throws EntityNotFoundException, AccountNotOnDemandException {
		final User userDb = userRepository.findById(user.getId()).orElse(null);
		if (userDb == null) {
			LOG.error("User with id {} not found", user.getId());
			throw new EntityNotFoundException(User.class, user.getId());
		}
		if ((userDb.isAccountRequestDemand() == null || !userDb.isAccountRequestDemand())
				&& (userDb.isExtensionRequestDemand() == null || !userDb.isExtensionRequestDemand())) {
			throw new AccountNotOnDemandException(user.getId());
		}
		// Confirm and update user
		if (userDb.isExtensionRequestDemand() != null && userDb.isExtensionRequestDemand()) {
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

	@Override
	public void deleteById(final Long id) throws EntityNotFoundException {
		final User user = (User) userRepository.findById(id).orElse(null);
		
		if (user == null) {
			throw new EntityNotFoundException(User.class, id);
		}
		List<AccessRequest> requests = this.accessRequestService.findByUserId(id);
		for (AccessRequest request : requests) {
			this.accessRequestService.deleteById(request.getId());
		}
		userRepository.deleteById(id);
		publisher.publishEvent(new UserDeleteEvent(id));
		
		try {
			ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DELETE_USER_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS);
			eventService.publishEvent(event);
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.DELETE_USER_QUEUE, mapper.writeValueAsString(event));
		} catch (Exception e) {
			LOG.error("Error while deleting user.");
		}

		keycloakClient.deleteUser(user.getKeycloakId());
	}

	@Override
	public void denyAccountRequest(final Long userId) throws EntityNotFoundException, AccountNotOnDemandException {
		final User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			throw new EntityNotFoundException(User.class, userId);
		}
		if ((user.isAccountRequestDemand() == null || !user.isAccountRequestDemand())
				&& (user.isExtensionRequestDemand() == null || !user.isExtensionRequestDemand())) {
			throw new AccountNotOnDemandException(userId);
		}
		if (user.isAccountRequestDemand() != null && user.isAccountRequestDemand()) {
			// Remove user
			//delete associated access requests
			List<AccessRequest> requests = this.accessRequestService.findByUserId(userId);
			for (AccessRequest request : requests) {
				this.accessRequestService.deleteById(request.getId());
			}
			
			userRepository.deleteById(userId);
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
	public List<User> findAccountRequests() {
		return Utils.toList(userRepository.findByAccountRequestDemandTrueOrExtensionRequestDemandTrue());
	}

	@Override
	public Optional<User> findByEmail(final String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public Optional<User> findByEmailForExtension(final String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User findById(final Long id) {
		return userRepository.findById(id).orElse(null);
	}

	@Override
	public Optional<User> findByUsername(final String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public Optional<User> findByUsernameForInvitation(final String username) {
		return userRepository.findByUsername(username);
	}
	
	@Override
	public List<User> getUsersToReceiveFirstExpirationNotification() {
		final LocalDate expirationDate = LocalDate.now().plusMonths(1);
		return userRepository.findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(expirationDate);
	}

	@Override
	public List<User> getUsersToReceiveSecondExpirationNotification() {
		final LocalDate expirationDate = LocalDate.now().plusWeeks(1);
		return userRepository.findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(expirationDate);
	}

	@Override
	public List<User> getExpiredUsers() {
		final LocalDate expirationDate = LocalDate.now();
		final LocalDate expirationDateLessOneWeek = expirationDate.minusWeeks(1);
		return userRepository.findByExpirationDateLessThanEqualAndExpirationDateGreaterThan(expirationDate, expirationDateLessOneWeek);
	}

	@Override
	public void requestExtension(Long userId, ExtensionRequestInfo requestInfo) throws EntityNotFoundException {
		final User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			throw new EntityNotFoundException(User.class, userId);
		}
		user.setExtensionRequestDemand(Boolean.TRUE);
		user.setExtensionRequestInfo(requestInfo);
		userRepository.save(user);

		// Send email to administrators
		emailService.notifyAdminAccountExtensionRequest(user);
	}

	@Override
	public User create(final User user) throws PasswordPolicyException, SecurityException {
		/* Password generation */
		final String newPassword = PasswordUtils.generatePassword();
		if (!PasswordUtils.checkPasswordPolicy(newPassword)) {
			throw new PasswordPolicyException();
		}
		
		User savedUser = userRepository.save(user);
		final String keycloakUserId = keycloakClient.createUserWithPassword(user, newPassword);
		savedUser.setKeycloakId(keycloakUserId); // Save keycloak id
		userRepository.save(savedUser);
		emailService.notifyCreateUser(savedUser, newPassword); // Send email to user
		return savedUser;
	}

	@Override
	public User createAccountRequest(final User user) throws PasswordPolicyException, SecurityException {
		/* Password generation */
		final String newPassword = PasswordUtils.generatePassword();
		if (!PasswordUtils.checkPasswordPolicy(newPassword)) {
			throw new PasswordPolicyException();
		}

		user.setRole(roleRepository.findByName("ROLE_USER")); // Set role 'USER'
		user.setExpirationDate(LocalDate.now().plusYears(1));

		accountRequestInfoRepository.save(user.getAccountRequestInfo()); // Save account request info
		User savedUser = userRepository.save(user);
		
		// Here, create a new access request
		AccessRequest accessRequest = new AccessRequest();
		accessRequest.setStatus(AccessRequest.ON_DEMAND);
		accessRequest.setStudyId(user.getAccountRequestInfo().getStudyId());
		if (user.getAccountRequestInfo().getStudyName() == null ) {
			String studyName = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.STUDY_NAME_QUEUE, accessRequest.getStudyId());
			accessRequest.setStudyName(studyName);
		} else {
			accessRequest.setStudyName(user.getAccountRequestInfo().getStudyName());
		}
		accessRequest.setUser(savedUser);
		accessRequest.setMotivation("User " + user.getFirstName() + " " +user.getLastName() 
				+ " created an account to join your study"
				+ (accessRequest.getMotivation() != null ? " with motivation text: " + accessRequest.getMotivation() : "")
				+ ". Associated email: " + user.getEmail());
		
		accessRequestRepository.save(accessRequest);
		
		emailService.notifyStudyManagerAccessRequest(accessRequest);

		final String keycloakUserId = keycloakClient.createUserWithPassword(user, newPassword);
		savedUser.setKeycloakId(keycloakUserId); // Save keycloak id
		userRepository.save(savedUser);
		emailService.notifyCreateAccountRequest(savedUser, newPassword); // Send email to user
		return savedUser;
	}

	@Override
	public List<IdName> findByIds(List<Long> userIdList) {
		final List<User> users = userRepository.findByIdIn(userIdList);
		List<IdName> result = new ArrayList<>();
		if (users != null) {
			for (User user : users) {
				result.add(new IdName(user.getId(), user.getUsername()));
			}
		}
		return result;
	}

	@Override
	public User update(final User user) throws EntityNotFoundException {
		final User userDb = userRepository.findById(user.getId()).orElse(null);
		if (userDb == null) {
			throw new EntityNotFoundException(User.class, user.getId());
		}
		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.UPDATE_USER_EVENT, user.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS);
		eventService.publishEvent(event);

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
	public void updateLastLogin(final String username) throws EntityNotFoundException {
		final User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));
		user.setLastLogin(LocalDate.now());
		userRepository.save(user);

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
	private User updateUserOnAllSystems(final User userDb, final User user) {
		if (user != null) {
			updateUserValues(userDb, user);
		}
		userRepository.save(userDb);
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
		userDb.setCanAccessToDicomAssociation(user.isCanAccessToDicomAssociation() != null && user.isCanAccessToDicomAssociation());
		userDb.setEmail(user.getEmail());
		// If expiration date was updated, reset expiration notifications.
		if (userDb.getExpirationDate() == null || user.getExpirationDate() == null || !userDb.getExpirationDate().isEqual(user.getExpirationDate())) {
			userDb.setFirstExpirationNotificationSent(Boolean.FALSE);
			userDb.setSecondExpirationNotificationSent(Boolean.FALSE);
		}
		userDb.setExpirationDate(user.getExpirationDate());
		userDb.setFirstName(user.getFirstName());
		userDb.setLastName(user.getLastName());
		userDb.setRole(user.getRole());
		userDb.setUsername(user.getUsername());
		return userDb;
	}
}
