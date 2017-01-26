package org.shanoir.ng.service;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.model.auth.UserContext;

/**
 * Authentication service.
 * 
 * @author msimon
 *
 */
public interface AuthenticationService {

	/**
	 * Authenticate an user with its login and password.
	 * 
	 * @param loginDTO user informations.
	 * @param response HTTP response.
	 * 
	 * @return connected user - null if connection fails.
	 * @throws ShanoirUsersException authentication exception.
	 */
	UserContext authenticate(LoginDTO loginDTO) throws ShanoirUsersException;
	
}
