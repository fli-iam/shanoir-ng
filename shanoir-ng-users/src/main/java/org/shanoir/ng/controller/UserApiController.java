package org.shanoir.ng.controller;

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
		// do some magic!
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<User> findUserById(@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId) {
		// do some magic!
		return new ResponseEntity<User>(HttpStatus.OK);
	}

	public ResponseEntity<List<User>> findUsers() {
		return new ResponseEntity<List<User>>(userService.finAll(), HttpStatus.OK);
	}

	public ResponseEntity<User> saveNewUser(@ApiParam(value = "the user to create", required = true) @RequestBody User user) {
		// do some magic!
		return new ResponseEntity<User>(HttpStatus.OK);
	}

	public ResponseEntity<Void> updateUser(
			@ApiParam(value = "id of the user", required = true) @PathVariable("userId") Long userId,
			@ApiParam(value = "the user to update", required = true) @RequestBody User user) {
		// do some magic!
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

}
