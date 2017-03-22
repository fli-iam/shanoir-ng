package org.shanoir.ng.role;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

/**
 * Implementation of custom repository for roles.
 * 
 * @author msimon
 *
 */
@Repository
public class RoleRepositoryImpl implements RoleRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllNames() {
		return entityManager.createQuery("SELECT r.name FROM Role r").getResultList();
	}

}
