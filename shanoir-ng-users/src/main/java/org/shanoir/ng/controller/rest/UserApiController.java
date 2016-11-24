package org.shanoir.ng.controller.rest;

import java.util.List;

import org.shanoir.ng.model.User;
import org.shanoir.ng.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class UserApiController implements UserApi {

	@Autowired
	private UserService userService;

	public ResponseEntity<Void> deleteUser(@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) {
		if (userService.findById(userId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		userService.deleteById(userId);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	public ResponseEntity<User> findUserById(@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) {
		User user = userService.findById(userId);
		if (user == null) {
			return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<User>(user, HttpStatus.OK);
		}
	}

	public ResponseEntity<List<User>> findUsers() {
		List<User> users = userService.findAll();
		if (users.isEmpty()) {
			return new ResponseEntity<List<User>>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<List<User>>(users, HttpStatus.OK);
		}
	}

	public ResponseEntity<User> saveNewUser(@ApiParam(value = "the user to create", required = true) @RequestBody User user) {
		userService.save(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	public ResponseEntity<Void> updateUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
			@ApiParam(value = "the user to update", required = true) @RequestBody User user) {
		userService.save(user);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

}
