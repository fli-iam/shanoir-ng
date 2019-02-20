package org.shanoir.ng.user.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.shanoir.ng.shared.controller.AbstractUserRequestApiController;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-16T08:28:10.257Z")

@Controller
public class LastLoginDateApiController extends AbstractUserRequestApiController implements LastLoginDateApi {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LastLoginDateApiController.class);

	public ResponseEntity<Void> lastLoginDate(
			@ApiParam(value = "username of user for last login date update", required = true) @RequestBody final String username,
			@Context final HttpServletRequest httpRequest) {
		try {
			// Update user login date
			getUserService().updateLastLogin(username);
		} catch (EntityNotFoundException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

}
