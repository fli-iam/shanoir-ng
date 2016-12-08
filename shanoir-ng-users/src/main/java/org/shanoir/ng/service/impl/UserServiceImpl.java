package org.shanoir.ng.service.impl;

import java.util.List;

import org.shanoir.ng.model.User;
import org.shanoir.ng.model.exception.ShanoirUsersException;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.repository.UserRepositorySpecific;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRepositorySpecific userRepositorySpecific;

	@Override
	public List<User> findAll() {
		return Utils.toList(userRepository.findAll());
	}

	@Override
	public User findById(Long id) {
		return userRepository.findOne(id);
	}

	@Override
	public void save(User user) {
		userRepository.save(user);
	}

	@Override
	public void updateFromShanoirOld(User user) throws ShanoirUsersException {
		if (user.getId() == null) {
			LOG.error("Cannot update an user without id.");
			return;
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
			ShanoirUsersException.logAndThrow(LOG, "Error updating user from Shanoir Old: " + e.getMessage());
		}
	}

	@Override
	public void deleteById(Long id) {
		userRepository.delete(id);
	}

	@Override
	public List<User> findBy(String fieldName, Object value) {
		return userRepositorySpecific.findBy(fieldName, value);
	}

}
