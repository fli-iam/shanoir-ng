package org.shanoir.ng.manufacturermodel.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.shared.core.model.IdName;

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
	public List<IdName> findIdsAndNames() {
		return em.createNativeQuery("SELECT id, name FROM manufacturer_model", "ManufacturerModelNameResult").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IdName> findIdsAndNamesForCenter(Long centerId) {
		return em.createNativeQuery("SELECT id, name FROM center ", "ManufacturerModelNameResult").getResultList();
	}

}
