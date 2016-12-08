package org.shanoir.ng.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserRepositorySpecificImpl implements UserRepositorySpecific {

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
