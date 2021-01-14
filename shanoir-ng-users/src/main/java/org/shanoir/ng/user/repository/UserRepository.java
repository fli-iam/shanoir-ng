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

package org.shanoir.ng.user.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Magic repository for users
 *
 * @author jlouis
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

	/**
	 * Find all users for a role.
	 * 
	 * @param roleName
	 *            role name.
	 * @return list of users.
	 */
	@Query("select email from User u where u.role.name='ROLE_ADMIN'")
	List<String> findAdminEmails();

	/**
	 * Find user by its email address
	 *
	 * @param email
	 * @return a user or null
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Find users who have account that will soon expire and have not received
	 * first notification.
	 * 
	 * @param expirationDate
	 *            expiration date to check.
	 * @return list of users.
	 */
	List<User> findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(LocalDate expirationDate);

	/**
	 * Find users who have account that will soon expire and have not received
	 * second notification.
	 * 
	 * @param expirationDate
	 *            expiration date to check.
	 * @return list of users.
	 */
	List<User> findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(LocalDate expirationDate);

	/**
	 * Find users who have account that expire today
	 * 
	 * @param expirationDate expiration date to check.
	 * @param expirationDateLessOneWeek {@link Expiration} date minus one week.
	 * @return list of expired users of less than one week.
	 */
	List<User> findByExpirationDateLessThanEqualAndExpirationDateGreaterThan(LocalDate expirationDate, LocalDate expirationDateLessOneWeek);

	/**
	 * Find users by their id.
	 * 
	 * @param userIdList
	 *            list of user ids.
	 * @return list of users.
	 */
	List<User> findByIdIn(List<Long> userIdList);
	
	/**
	 * Find user by its username
	 *
	 * @param id
	 * @return a user or null
	 */
	Optional<User> findByUsername(String username);

}
