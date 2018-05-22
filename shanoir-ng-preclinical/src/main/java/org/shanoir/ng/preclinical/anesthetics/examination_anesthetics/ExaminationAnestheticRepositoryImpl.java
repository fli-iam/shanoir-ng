package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;


@Component
public class ExaminationAnestheticRepositoryImpl implements ExaminationAnestheticRepositoryCustom{

	@PersistenceContext
    private EntityManager em;
		
	@SuppressWarnings("unchecked")
	@Override
	public List<ExaminationAnesthetic> findBy(String fieldName, Object value) {
		return em.createQuery(
				"SELECT ea FROM ExaminationAnesthetic ea WHERE ea." + fieldName + " LIKE :value")
				.setParameter("value", value)
				.getResultList();
	}
	
}
