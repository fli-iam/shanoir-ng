package org.shanoir.ng.controller.rest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.shanoir.ng.exception.ShanoirUsersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-16T08:28:10.257Z")

@Controller
public class LoginApiController extends AbstractUserRequestApiController implements LoginApi {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(LoginApiController.class);

	public ResponseEntity<Void> login(
			@ApiParam(value = "username of user for login date update", required = true) @RequestBody final String username,
			@Context final HttpServletRequest httpRequest) {
		try {
			final InetAddress address = InetAddress.getByName("shanoir-ng-keycloak");
			if (!httpRequest.getRemoteAddr().equals(address.getHostAddress())) {
				LOG.error("Request does not come from local keycloak server.");
				return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
			}
		} catch (UnknownHostException e) {
			LOG.error("Error while getting local keycloak server address", e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try {
			// Update user login date
			getUserService().updateLastLogin(username);
		} catch (ShanoirUsersException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

}
