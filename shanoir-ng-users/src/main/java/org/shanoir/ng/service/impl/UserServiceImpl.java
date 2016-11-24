package org.shanoir.ng.service.impl;

import java.util.List;

import org.shanoir.ng.model.User;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.service.repository.UserRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

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
	public void deleteById(Long id) {
		userRepository.delete(id);
	}

}
