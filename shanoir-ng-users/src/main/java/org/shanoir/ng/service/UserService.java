package org.shanoir.ng.service;

import java.util.List;

import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.validation.UniqueCheckableService;

/**
 * User service.
 *
 * @author msimon
 * @author jlouis
 *
 */
public interface UserService extends UniqueCheckableService<User> {

	/**
	 * Delete a user
	 * @param id
	 */
	void deleteById(Long id);
	
    /**
     * Get all the users
     * @return a list of users
     */
    List<User> findAll();

    /**
     * Find user by its id
     *
     * @param id
     * @return a user or null
     */
    User findById(Long id);

	/**
	 * Handles an account request.
	 * 
	 * @param userId user id.
	 * @param acceptRequest answer acceptance (accept or deny).
	 * @throws ShanoirUsersException
	 */
	void handleAccountRequest(Long userId, boolean acceptRequest) throws ShanoirUsersException;
	
    /**
     * Save a user.
     *
     * @param user user to create.
     * @return created user.
	 * @throws ShanoirUsersException
     */
    User save(User user) throws ShanoirUsersException;

    /**
     * Update a user.
     *
     * @param user user to update.
     * @return updated user.
     * @throws ShanoirUsersException
     */
    User update(User user) throws ShanoirUsersException;
    
    /**
     * Update a user from the old Shanoir
     * @param user
     * @throws ShanoirUsersException
     */
    void updateFromShanoirOld(User user) throws ShanoirUsersException;

}
