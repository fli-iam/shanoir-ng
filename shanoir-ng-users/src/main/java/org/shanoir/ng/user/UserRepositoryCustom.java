package org.shanoir.ng.user;

import java.util.List;

/**
 * Custom repository for users.
 * 
 * @author msimon
 *
 */
public interface UserRepositoryCustom {

	/**
	 * Find users by field value.
	 * 
	 * @param fieldName searched field name.
	 * @param value value.
	 * @return list of users.
	 */
	List<User> findBy(String fieldName, Object value);

}
