package org.shanoir.ng.repository;

import java.util.List;

/**
 * Custom repository for roles.
 * 
 * @author msimon
 *
 */
public interface RoleRepositoryCustom {

	/**
	 * Get list of roles name.
	 * 
	 * @return list of names.
	 */
	List<String> getAllNames();

}
