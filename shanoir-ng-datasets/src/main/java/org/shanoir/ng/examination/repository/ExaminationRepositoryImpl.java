package org.shanoir.ng.examination.repository;

import java.util.List;

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.paging.PageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Component
public class ExaminationRepositoryImpl implements ExaminationRepositoryCustom {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;


	@Override
	public Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, Pageable pageable, Boolean preclinical) {
		
		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, pageable, preclinical, null);
		return new PageImpl<Examination>(pair.getFirst(), pageable, pair.getSecond()); 
	}
	
	@Override
	public Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, Pageable pageable) {
		
		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, pageable, null, null);
		return new PageImpl<Examination>(pair.getFirst(), pageable, pair.getSecond()); 
	}
	
	@Override
	public Page<Examination> findPageByStudyCenterOrStudyIdInAndSubjectName(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, String subjectName, Pageable pageable) {

		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, pageable, null, subjectName);
		return new PageImpl<Examination>(pair.getFirst(), pageable, pair.getSecond()); 
	}
	
	@Override
	public List<Examination> findAllByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds) {
		
		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, null, null, null);
		return pair.getFirst();
	}
	
	@SuppressWarnings("unchecked")
	private Pair<List<Examination>, Long> find(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, Pageable pageable, Boolean preclinical, String subjectName) {
		
		String queryEndStr = "from Examination as ex ";
		int nbPreParams = 1;
		int preclinicalIndex = -1;
		int subjectNameIndex = -1;
		if (preclinical != null) {
			nbPreParams++;
			preclinicalIndex = nbPreParams;
			queryEndStr +=  "where ex.preclinical is ?" + preclinicalIndex + " ";
		}
		if (subjectName != null) {
			nbPreParams++;
			subjectNameIndex = nbPreParams;
			queryEndStr +=  "and ex.subject.name is ?" + subjectNameIndex + " ";
		} 
		queryEndStr += "and (ex.study.id in ?1 ";
		
		int i = nbPreParams + 1;
		for (@SuppressWarnings("unused") Pair<Long, Long> studyCenter : studyCenterIds) {
			queryEndStr += "or (ex.study.id = ?" + i + " and ex.centerId = ?" + (i + 1) + ") ";
			i += 2;
		}
		queryEndStr += ") ";
		
		String queryStr = "select ex " + queryEndStr;
		
		if (pageable != null && pageable.getSort() != null && pageable.getSort().isSorted()) {
			queryStr += "order by ";
			int isort = 0;
			for (Sort.Order order : pageable.getSort()) {
				if (isort >= 1) {
					queryStr += ",";
				}
				queryStr += "ex." + order.getProperty();
				queryStr += " " + order.getDirection() + " ";
				isort ++;
			}
		}
		
		LOG.debug("examination paging hql query : " + queryStr);
		
		Query query = entityManager.createQuery(queryStr);
		
		query.setParameter(1, studyIds);
		if (preclinical != null) {
			query.setParameter(preclinicalIndex, preclinical);
		}
		if (subjectName != null) {
			query.setParameter(subjectNameIndex, subjectName);
		}
		i = nbPreParams + 1;
		for (Pair<Long, Long> studyCenter : studyCenterIds) {
			query.setParameter(i, studyCenter.getFirst());
			query.setParameter(i + 1, studyCenter.getSecond());
			i += 2;
		}
		
		Long total = null;
		if (pageable != null) {
			String queryCountStr = "select count(ex) " + queryEndStr;
			Query queryCount = entityManager.createQuery(queryCountStr);			
			queryCount.setParameter(1, studyIds);
			if (preclinical != null) {
				queryCount.setParameter(preclinicalIndex, preclinical);
			}
			if (subjectName != null) {
				queryCount.setParameter(subjectNameIndex, subjectName);
			}
			i = nbPreParams + 1;
			for (Pair<Long, Long> studyCenter : studyCenterIds) {
				queryCount.setParameter(i, studyCenter.getFirst());
				queryCount.setParameter(i + 1, studyCenter.getSecond());
				i += 2;
			}
			total = (Long) queryCount.getSingleResult();
			query.setFirstResult(Math.toIntExact(pageable.getPageNumber() * pageable.getPageSize()));
			query.setMaxResults(pageable.getPageSize());
		}
		
		LOG.debug("examination paging query : " + query);

		return new Pair<>(query.getResultList(), total);
	}
	
}
