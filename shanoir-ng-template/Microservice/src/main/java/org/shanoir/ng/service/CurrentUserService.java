package org.shanoir.ng.service;

/**
 * Service used to manage connected user.
 * 
 * @author msimon
 *
 */
public interface CurrentUserService {

	/**
	 * Check if user can access to a resource. Example to retrieve connected
	 * user and to get information about it.
	 * 
	 * @param userId
	 *            user id.
	 * @return true if access ok.
	 */
	boolean canAccessUser(Long userId);

}
