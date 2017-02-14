package org.shanoir.ng.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.dto.ShanoirOldUserDTO;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.repository.AccountRequestInfoRepository;
import org.shanoir.ng.repository.RoleRepository;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.PasswordUtils;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User service implementation.
 * 
 * @author msimon
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
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public User confirmAccountRequest(final Long userId, final User user) throws ShanoirUsersException {
		final User userDb = userRepository.findOne(userId);
		if (userDb == null) {
			LOG.error("User with id " + userId + " not found");
			throw new ShanoirUsersException(ErrorModelCode.USER_NOT_FOUND);
		}
		if (!userDb.isAccountRequestDemand()) {
			LOG.error("User with id " + userId + " has no account request");
			throw new ShanoirUsersException(ErrorModelCode.NO_ACCOUNT_REQUEST);
		}
		userDb.setAccountRequestDemand(false);
		updateUserValues(userDb, user);
		try {
			userRepository.save(userDb);
		} catch (Exception e) {
			ShanoirUsersException.logAndThrow(LOG,
					"Error while confirming user account request with id '" + userId + "': " + e.getMessage());
		}
		return userDb;
	}

	@Override
	public void deleteById(final Long id) throws ShanoirUsersException {
		final UserContext currentUser = (UserContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (currentUser != null && id.equals(currentUser.getId())) {
			ShanoirUsersException.logAndThrow(LOG, "Forbidden to delete connected user.");
		}
		userRepository.delete(id);
		deleteUserOnShanoirOld(id);
	}

	/*
	 * Send a message to Shanoir old to delete an user.
	 * 
	 * @param userId user id.
	 */
	private void deleteUserOnShanoirOld(final Long userId) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.deleteQueueOut().getName(),
					new ObjectMapper().writeValueAsString(userId));
		} catch (AmqpException e) {
			LOG.error("Cannot send user " + userId + " delete to Shanoir Old on queue : "
					+ RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send user " + userId + " userId because of an error while serializing user.", e);
		}
	}

	@Override
	public void denyAccountRequest(final Long userId) throws ShanoirUsersException {
		final User user = userRepository.findOne(userId);
		if (user == null) {
			LOG.error("User with id " + userId + " not found");
			throw new ShanoirUsersException(ErrorModelCode.USER_NOT_FOUND);
		}
		if (!user.isAccountRequestDemand()) {
			LOG.error("User with id " + userId + " has no account request");
			throw new ShanoirUsersException(ErrorModelCode.NO_ACCOUNT_REQUEST);
		}
		// Remove user
		userRepository.delete(userId);
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
	public User save(final User user) throws ShanoirUsersException {
		String newPassword = null;
		if (!StringUtils.hasText(user.getPassword())) {
			newPassword = PasswordUtils.generatePassword();
			// TODO: send email
		} else {
			newPassword = user.getPassword();
			// Check password
			PasswordUtils.checkPasswordPolicy(newPassword, user.getUsername());
		}
		// Save hashed password
		user.setPassword(PasswordUtils.getHash(newPassword));
		User savedUser = null;
		try {
			if (user.getAccountRequestInfo() != null) {
				// Save account request info
				accountRequestInfoRepository.save(user.getAccountRequestInfo());
				user.setAccountRequestDemand(true);
				// Set role 'guest'
				user.setRole(roleRepository.findByName("guestRole")
						.orElseThrow(() -> new ShanoirUsersException("Error while getting role 'guestRole'")));
			}
			savedUser = userRepository.save(user);
		} catch (DataIntegrityViolationException dive) {
			ShanoirUsersException.logAndThrow(LOG, "Error while creating user: " + dive.getMessage());
		}
		updateShanoirOld(savedUser);
		return savedUser;
	}

	@Override
	public User update(final User user) throws ShanoirUsersException {
		final User userDb = userRepository.findOne(user.getId());
		updateUserValues(userDb, user);
		try {
			userRepository.save(userDb);
		} catch (Exception e) {
			ShanoirUsersException.logAndThrow(LOG, "Error while updating user: " + e.getMessage());
		}
		updateShanoirOld(userDb);
		return userDb;
	}

	@Override
	public void updateFromShanoirOld(final User user) throws ShanoirUsersException {
		if (user.getId() == null) {
			if (user.isAccountRequestDemand()) {
				// User account request
				// Set role 'guest'
				user.setRole(roleRepository.findByName("guestRole")
						.orElseThrow(() -> new ShanoirUsersException("Error while getting role 'guestRole'")));
				try {
					if (user.getAccountRequestInfo() != null) {
						// Save account request info
						accountRequestInfoRepository.save(user.getAccountRequestInfo());
					}
					// Save user
					userRepository.save(user);
				} catch (Exception e) {
					ShanoirUsersException.logAndThrow(LOG,
							"Error while creating user from Shanoir Old: " + e.getMessage());
				}
			} else {
				throw new IllegalArgumentException("User id cannot be null if not an account request");
			}
		} else {
			final User userDb = userRepository.findOne(user.getId());
			if (userDb != null) {
				updateUserValues(userDb, user);
				try {
					userRepository.save(userDb);
				} catch (Exception e) {
					ShanoirUsersException.logAndThrow(LOG,
							"Error while updating user from Shanoir Old: " + e.getMessage());
				}
			}
		}
	}

	@Override
	public void updateLastLogin(final User user) {
		user.setLastLogin(new Date());
		try {
			userRepository.save(user);
		} catch (Exception e) {
			LOG.error("Error while updating last login date for user " + user.getId(), e);
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param user user.
	 * 
	 * @return false if it fails, true if it succeed.
	 */
	private boolean updateShanoirOld(final User user) {
		// Parse user to old Shanoir user entity.
		final ShanoirOldUserDTO shanoirOldUser = new ShanoirOldUserDTO();
		shanoirOldUser.setId(user.getId());
		shanoirOldUser.setCanAccessToDicomAssociation(user.isCanAccessToDicomAssociation());
		shanoirOldUser.setCreatedOn(user.getCreationDate());
		shanoirOldUser.setEmail(user.getEmail());
		shanoirOldUser.setExpirationDate(user.getExpirationDate());
		shanoirOldUser.setFirstExpirationNotificationSent(user.isFirstExpirationNotificationSent());
		shanoirOldUser.setFirstName(user.getFirstName());
		shanoirOldUser.setIsMedical(user.isMedical());
		shanoirOldUser.setLastLoginOn(user.getLastLogin());
		shanoirOldUser.setLastName(user.getLastName());
		shanoirOldUser.setPasswordHash(user.getPassword());
		shanoirOldUser.setRole(user.getRole());
		shanoirOldUser.setSecondExpirationNotificationSent(user.isSecondExpirationNotificationSent());
		shanoirOldUser.setUsername(user.getUsername());

		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(),
					new ObjectMapper().writeValueAsString(shanoirOldUser));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send user " + user.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send user " + user.getId() + " save/update because of an error while serializing user.",
					e);
		}
		return false;
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
		// TODO: password modification
		// TODO: add motivation (user account request)
		userDb.setRole(user.getRole());
		userDb.setMedical(user.isMedical());
		userDb.setUsername(user.getUsername());
		return userDb;
	}

}
