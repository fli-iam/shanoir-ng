package org.shanoir.ng.service.impl;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.shanoir.ng.model.User;
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
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private UserRepository userRepository;

	@Override
	public void deleteById(final Long id) {
		userRepository.delete(id);
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
	public User findById(final Long id) {
		return userRepository.findOne(id);
	}

	@Override
	public void handleAccountRequest(final Long userId, final boolean acceptRequest) throws ShanoirUsersException {
		final User user = userRepository.findOne(userId);
		if (user == null) {
			LOG.error("User with id " + userId + " not found");
			throw new ShanoirUsersException(ErrorModelCode.USER_NOT_FOUND);
		}
		if (acceptRequest) {
			user.setAccountRequestDemand(false);
			try {
				userRepository.save(user);
			} catch (Exception e) {
				ShanoirUsersException.logAndThrow(LOG,
						"Error while confirming user account request with id '" + userId + "': " + e.getMessage());
			}
		} else {
			// TODO: what to do? remove user?
		}
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
		try {
			userRepository.save(userDb);
		} catch (Exception e) {
			ShanoirUsersException.logAndThrow(LOG, "Error while updating user: " + e.getMessage());
		}
		return userDb;
	}

	@Override
	public void updateFromShanoirOld(final User user) throws ShanoirUsersException {
		if (user.getId() == null) {
			throw new IllegalArgumentException("user id cannot be null");
		}

		final User userDb = userRepository.findOne(user.getId());
		userDb.setCanAccessToDicomAssociation(user.isCanAccessToDicomAssociation());
		userDb.setEmail(user.getEmail());
		userDb.setExpirationDate(user.getExpirationDate());
		userDb.setFirstName(user.getFirstName());
		userDb.setLastName(user.getLastName());
		userDb.setPassword(user.getPassword());
		userDb.setRole(user.getRole());
		userDb.setMedical(user.isMedical());
		try {
			userRepository.save(userDb);
		} catch (Exception e) {
			ShanoirUsersException.logAndThrow(LOG, "Error while updating user from Shanoir Old: " + e.getMessage());
		}
	}

	/*
	 * Update Shanoir Old.
	 * 
	 * @param user
	 * 
	 * @return false if it fails, true if it succeed
	 */
	private boolean updateShanoirOld(final User user) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(),
					new ObjectMapper().writeValueAsString(user));
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

}
