package org.shanoir.ng.service.impl;

import java.util.List;

import org.shanoir.ng.model.User;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.service.repository.UserRepository;
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
	public void updateFromShanoirOld(User user) {
		if (user.getId() == null) {
			LOG.error("Cannot update an user without id.");
			return;
		}
		
		final User userDb = userRepository.findOne(user.getId());
		try {
			userDb.setCanAccessToDicomAssociation(user.getCanAccessToDicomAssociation());
			userDb.setEmail(user.getEmail());
			userDb.setExpirationDate(user.getExpirationDate());
			userDb.setFirstName(user.getFirstName());
			userDb.setLastName(user.getLastName());
			userDb.setMotivation(user.getMotivation());
			userDb.setPassword(user.getPassword());
			userDb.setRole(user.getRole());
			userDb.setIsMedical(user.getIsMedical());
		} catch (Exception e) {
			LOG.error("Error updating user from Shanoir Old: " + e.getMessage());
		}
		
		userRepository.save(userDb);
	}
	
	@Override
	public void deleteById(Long id) {
		userRepository.delete(id);
	}

}
