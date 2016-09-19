package org.shanoir.ng.service;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;

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
	 * @throws Exception authentication exception
	 */
	UserDTO authenticate(LoginDTO loginDTO, HttpServletResponse response) throws Exception;
	
    /**
     * Logout a user:
     * - Clear the Spring Security context
     * - Remove the stored UserDTO secret
     */
    void logout();
    
}
