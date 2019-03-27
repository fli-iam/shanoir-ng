package org.shanoir.ng.utils;

import java.time.LocalDate;

import org.shanoir.ng.role.model.Role;
import org.shanoir.ng.user.model.User;

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
	public static final String ADMIN_ROLE_DISPLAY_NAME = "admin";
	public static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
	
	public static final Long EXPERT_ROLE_ID = 3L;
	public static final String EXPERT_ROLE_DISPLAY_NAME = "expert";
	public static final String EXPERT_ROLE_NAME = "ROLE_EXPERT";
	
	public static final Long USER_ROLE_ID = 2L;
	public static final String USER_ROLE_DISPLAY_NAME = "user";
	public static final String USER_ROLE_NAME = "ROLE_USER";
	
	// User data
	public static final String USER_EMAIL = "toto@to.to";
	public static final String USER_FIRSTNAME = "Toto";
	public static final String USER_LASTNAME = "Toto";
	
	// Login/password
	public static final String NEW_USER_LOGIN = "user";
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
		role.setDisplayName(ADMIN_ROLE_DISPLAY_NAME);
		role.setName(ADMIN_ROLE_NAME);
		return role;
	}
	
	/**
	 * Create a role 'expert'.
	 * 
	 * @return role
	 */
	public static Role createExpertRole() {
		final Role role = new Role();
		role.setId(EXPERT_ROLE_ID);
		role.setDisplayName(EXPERT_ROLE_DISPLAY_NAME);
		role.setName(EXPERT_ROLE_NAME);
		return role;
	}
	
	/**
	 * Create a role 'user'.
	 * 
	 * @return role
	 */
	public static Role createUserRole() {
		final Role role = new Role();
		role.setId(USER_ROLE_ID);
		role.setDisplayName(USER_ROLE_DISPLAY_NAME);
		role.setName(USER_ROLE_NAME);
		return role;
	}
	
	/**
	 * Create a user.
	 * 
	 * @return user.
	 */
	public static User createAdmin() {
		return createUser(createAdminRole(), null);
	}
	
	/**
	 * Create a user.
	 * 
	 * @return user.
	 */
	public static User createAdmin(Long id) {
		return createUser(createAdminRole(), id);
	}
	
	/**
	 * Create a user.
	 * 
	 * @return user.
	 */
	public static User createUser() {
		return createUser(createUserRole(), null);
	}
	
	/**
	 * Create a user.
	 * 
	 * @return user.
	 */
	public static User createUser(Long id) {
		return createUser(createUserRole(), id);
	}
	
	/**
	 * Create a user.
	 * 
	 * @return user.
	 */
	public static User createExpert() {
		return createUser(createExpertRole(), null);
	}
	
	/**
	 * Create a user.
	 * 
	 * @return user.
	 */
	public static User createExpert(Long id) {
		return createUser(createExpertRole(), id);
	}
	
	/**
	 * Create a user with a defined role.
	 * 
	 * @param role role.
	 * @return user.
	 */
	public static User createUser(final Role role, final Long id) {
		final User user = new User();
		user.setCreationDate(LocalDate.now());
		user.setEmail(USER_EMAIL);
		user.setExpirationDate(LocalDate.now().plusYears(1));
		user.setFirstName(USER_FIRSTNAME);
		user.setLastName(USER_LASTNAME);
		user.setRole(role);
		user.setUsername(NEW_USER_LOGIN);
		user.setId(id);
		return user;
	}
	
}
