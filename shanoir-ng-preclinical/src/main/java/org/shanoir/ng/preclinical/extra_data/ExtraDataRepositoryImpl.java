package org.shanoir.ng.preclinical.extra_data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.springframework.stereotype.Component;

@Component
public class ExtraDataRepositoryImpl implements ExtraDataRepositoryCustom<ExaminationExtraData> {

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("unchecked")
	@Override
	public List<ExaminationExtraData> findBy(String fieldName, Object value) {
		return em.createQuery("SELECT ex FROM ExaminationExtraData ex WHERE ex." + fieldName + " LIKE :value")
				.setParameter("value", value).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExaminationExtraData> findAllByExaminationId(Long id) {
		return em.createQuery("SELECT ex FROM ExaminationExtraData ex WHERE ex.examinationId" + " LIKE :id")
				.setParameter("id", id).getResultList();
	}

}
