package org.shanoir.ng.manufacturermodel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for manufacturers.
 * 
 * @author msimon
 *
 */
@Component
public class ManufacturerRepositoryImpl implements ManufacturerRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Manufacturer> findBy(String fieldName, Object value) {
		return em.createQuery("SELECT m FROM Manufacturer m WHERE m." + fieldName + " LIKE :value")
				.setParameter("value", value).getResultList();
	}

}
