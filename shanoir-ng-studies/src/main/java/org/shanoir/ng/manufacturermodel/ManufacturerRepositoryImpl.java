package org.shanoir.ng.manufacturermodel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.model.ItemRepositoryCustom;
import org.springframework.stereotype.Component;

/**
 * Implementation of custom repository for manufacturers.
 * 
 * @author msimon
 *
 */
@Component
public class ManufacturerRepositoryImpl implements ItemRepositoryCustom<Manufacturer> {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<Manufacturer> findBy(String fieldName, Object value) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT m FROM Manufacturer m WHERE m.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}

}
