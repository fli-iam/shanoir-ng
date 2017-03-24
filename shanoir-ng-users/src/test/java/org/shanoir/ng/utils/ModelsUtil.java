package org.shanoir.ng.utils;

import java.util.Date;

import org.shanoir.ng.role.Role;
import org.shanoir.ng.user.User;

/**
 * Utility class for test.
 * Generates models.
 * 
 * @author msimon
 *
 */
public final class ModelsUtil {

	// Role data
	public static final Long ADMIN_ROLE_ID = 1L;
	public static final int ADMIN_ROLE_ACCESS_LEVEL = 1;
	public static final String ADMIN_ROLE_DISPLAY_NAME = "admin";
	public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
	public static final Long ROLE_ID = 2L;
	public static final int ROLE_ACCESS_LEVEL = 3;
	public static final String ROLE_DISPLAY_NAME = "guest";
	public static final String ROLE_NAME = "ROLE_GUEST";
	
	// User data
	public static final String USER_EMAIL = "toto@to.to";
	public static final String USER_FIRSTNAME = "toto";
	public static final String USER_LASTNAME = "toto";
	
	// Login/password
	public static final String NEW_USER_LOGIN = "user";
	public static final String NEW_USER_PASSWORD = "password";
	public static final Long USER_ID = 1L;
	public static final String USER_LOGIN = "admin";
	public static final String USER_PASSWORD = "D0-483351E2-30";
	public static final String USER_LOGIN_GUEST = "wopa";
	public static final String USER_PASSWORD_GUEST = "1117DC-36DE-34";
	
	/**
	 * Create a role 'admin'.
	 * 
	 * @return role
	 */
	public static Role createAdminRole() {
		final Role role = new Role();
		role.setId(ADMIN_ROLE_ID);
		role.setAccessLevel(ADMIN_ROLE_ACCESS_LEVEL);
		role.setDisplayName(ADMIN_ROLE_DISPLAY_NAME);
		role.setName(ADMIN_ROLE_NAME);
		return role;
	}
	
	/**
	 * Create a role 'guest'.
	 * 
	 * @return role
	 */
	public static Role createGuestRole() {
		final Role role = new Role();
		role.setId(ROLE_ID);
		role.setAccessLevel(ROLE_ACCESS_LEVEL);
		role.setDisplayName(ROLE_DISPLAY_NAME);
		role.setName(ROLE_NAME);
		return role;
	}
	
	/**
	 * Create a user.
	 * 
	 * @return user.
	 */
	public static User createUser() {
		return createUser(createGuestRole());
	}
	
	/**
	 * Create a user with a defined role.
	 * 
	 * @param role role.
	 * @return user.
	 */
	public static User createUser(final Role role) {
		final User user = new User();
		user.setCreationDate(new Date());
		user.setEmail(USER_EMAIL);
		user.setFirstName(USER_FIRSTNAME);
		user.setLastName(USER_LASTNAME);
		user.setPassword(NEW_USER_PASSWORD);
		user.setRole(role);
		user.setUsername(NEW_USER_LOGIN);
		return user;
	}
	
}
