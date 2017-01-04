package org.shanoir.ng.service.impl;

import java.util.List;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.exception.ShanoirUsersException;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.repository.UserRepositorySpecific;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserServiceImpl implements UserService {
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRepositorySpecific userRepositorySpecific;

	@Override
	public void deleteById(Long id) {
		userRepository.delete(id);
	}
	
	@Override
	public List<User> findAll() {
		return Utils.toList(userRepository.findAll());
	}

	@Override
	public List<User> findBy(String fieldName, Object value) {
		return userRepositorySpecific.findBy(fieldName, value);
	}
	
	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User findById(Long id) {
		return userRepository.findOne(id);
	}

	@Override
	public User findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	@Override
	public User save(User user) {
		User savedUser = userRepository.save(user);
		updateShanoirClassic(savedUser);
		return savedUser;
	}

	@Override
	public void updateFromShanoirOld(User user) throws ShanoirUsersException {
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

	@Override
	public User update(User user) throws ShanoirUsersException {
		final User userDb = userRepository.findOne(user.getId());
		userDb.setCanAccessToDicomAssociation(user.isCanAccessToDicomAssociation());
		userDb.setEmail(user.getEmail());
		userDb.setExpirationDate(user.getExpirationDate());
		userDb.setFirstName(user.getFirstName());
		userDb.setLastName(user.getLastName());
		// TODO: add motivation (user account request)
		userDb.setPassword(user.getPassword());
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

	/**
	 * Update Shanoir Classic
	 * @param user
	 * @return false if it fails, true if it succeed
	 */
	private boolean updateShanoirClassic(User user) {
		try {
			LOG.info("Send update to shanoir classic");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(), new ObjectMapper().writeValueAsString(user));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send user " + user.getId() + " save/update to Shanoir Classic on queue : " + RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send user " + user.getId() + " save/update because of an error while serialize user. ", e);
		}
		return false;
	}

}
