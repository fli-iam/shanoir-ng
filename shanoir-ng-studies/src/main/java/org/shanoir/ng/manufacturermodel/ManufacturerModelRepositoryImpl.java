package org.shanoir.ng.manufacturermodel;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Implementation of custom repository for centers.
 * 
 * @author msimon
 *
 */
public class ManufacturerModelRepositoryImpl implements ManufacturerModelRepositoryCustom {

	@PersistenceContext
	private EntityManager em;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ManufacturerModel> findBy(String fieldName, Object value) {
		final StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT mm FROM manufacturer_model mm WHERE mm.").append(fieldName).append(" LIKE :value");
		return em.createQuery(sqlQuery.toString()).setParameter("value", value).getResultList();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return em.createNativeQuery("SELECT id, name FROM manufacturer_model", "ManufacturerModelNameResult").getResultList();
	}



}
