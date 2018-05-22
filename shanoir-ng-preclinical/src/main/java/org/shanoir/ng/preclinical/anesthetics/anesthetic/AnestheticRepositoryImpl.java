package org.shanoir.ng.preclinical.anesthetics.anesthetic;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Component;


@Component
public class AnestheticRepositoryImpl implements AnestheticRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<Anesthetic> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT a FROM Anesthetic a WHERE a." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
	
	
}
