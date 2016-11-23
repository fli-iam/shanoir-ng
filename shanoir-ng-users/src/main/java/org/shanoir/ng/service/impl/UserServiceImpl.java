package org.shanoir.ng.service.impl;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
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
	public UserDTO authenticate(LoginDTO loginDTO, HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<User> finAll() {
		return Utils.toList(userRepository.findAll());
	}

}
