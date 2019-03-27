package org.shanoir.ng.user.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.core.repository.CustomRepository;
import org.shanoir.ng.shared.core.repository.CustomRepositoryImpl;
import org.shanoir.ng.user.model.User;
import org.springframework.stereotype.Repository;

/**
 * Implementation of custom repository for users.
 * 
 * @author msimon
 *
 */
@Repository
public class UserRepositoryImpl extends CustomRepositoryImpl<User> implements CustomRepository<User> {

	@PersistenceContext
	private EntityManager em;
	
}
