package org.shanoir.ng.controller.rest;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.exception.RestServiceException;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.exception.error.ErrorModel;
import org.shanoir.ng.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user authentication.
 *
 * @author msimon
 *
 */
@RestController
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	@ResponseBody
	public UserDTO authenticate(@RequestBody final LoginDTO loginDTO, final HttpServletResponse response)
			throws RestServiceException {
		try {
			return authenticationService.authenticate(loginDTO, response);
		} catch (ShanoirUsersException sue) {
			throw new RestServiceException(new ErrorModel(401, "" + sue.getErrorCode(), null));
		}
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(final HttpSession session) {
		session.invalidate();
	}

}
