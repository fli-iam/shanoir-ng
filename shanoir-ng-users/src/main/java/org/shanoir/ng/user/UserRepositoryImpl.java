package org.shanoir.ng.user;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

/**
 * Implementation of custom repository for users.
 * 
 * @author msimon
 *
 */
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

	@PersistenceContext
    private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT u FROM User u WHERE u." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}

}
