package org.shanoir.ng.service;

import java.util.List;

import org.shanoir.ng.model.User;

/**
 * User service.
 *
 * @author msimon
 * @author jlouis
 *
 */
public interface UserService {

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
     * Save a user
     * @param user
     */
    void save(User user);
    
    /**
     * Update a user from the old Shanoir
     * @param user
     */
    void updateFromShanoirOld(User user);

    /**
     * Delete a user
     * @param id
     */
    void deleteById(Long id);

}
