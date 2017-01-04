package org.shanoir.ng.service;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.exception.ShanoirUsersException;

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
	UserDTO authenticate(LoginDTO loginDTO, HttpServletResponse response) throws ShanoirUsersException;
	
    /**
     * Logout a user:
     * - Clear the Spring Security context
     * - Remove the stored UserDTO secret
     */
    void logout();
    
}
