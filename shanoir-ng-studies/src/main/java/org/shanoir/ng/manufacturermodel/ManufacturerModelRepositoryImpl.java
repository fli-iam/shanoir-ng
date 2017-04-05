package org.shanoir.ng.manufacturermodel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for manufacturer models.
 * 
 * @author msimon
 *
 */
@Component
public class ManufacturerModelRepositoryImpl implements ManufacturerModelRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<ManufacturerModel> findBy(String fieldName, Object value) {
		return em.createQuery("SELECT mm FROM ManufacturerModel mm WHERE mm." + fieldName + " LIKE :value")
				.setParameter("value", value).getResultList();
	}

}
