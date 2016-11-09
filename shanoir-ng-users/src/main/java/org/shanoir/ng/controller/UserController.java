package org.shanoir.ng.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for users.
 * 
 * @author msimon
 *
 */
@RestController
public class UserController {

	@RequestMapping("/user")
	@ResponseBody
	public Principal user(final Principal user) {
		return user;
	}

}
