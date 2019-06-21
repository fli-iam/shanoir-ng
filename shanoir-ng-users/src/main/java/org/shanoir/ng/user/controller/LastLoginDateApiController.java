/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

<<<<<<< HEAD:shanoir-ng-users/src/main/java/org/shanoir/ng/user/controller/LastLoginDateApiController.java
package org.shanoir.ng.user.controller;
=======
package org.shanoir.ng.user;
>>>>>>> upstream/develop:shanoir-ng-users/src/main/java/org/shanoir/ng/user/LastLoginDateApiController.java

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
