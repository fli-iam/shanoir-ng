package org.shanoir.ng.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.model.User;

/**
 * Authentication service.
 *
 * @author msimon
 * @author jlouis
 *
 */
public interface UserService {

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

    /**
     * Get all the users
     * @return a list of users
     */
    List<User> finAll();

}
