package org.shanoir.ng.examination.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.math3.util.Pair;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ExaminationRepositoryImpl implements ExaminationRepositoryCustom {

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
		
		String queryEndStr = "from Examination as ex "
				+ (preclinical != null ? "where ex.preclinical is ?2 " : " ")
				+ (subjectName != null ? "and ex.subject.name is ?3" : " ")
				+ "and (ex.studyId in ?1 ";
		int i = 2;
		for (@SuppressWarnings("unused") Pair<Long, Long> studyCenter : studyCenterIds) {
			queryEndStr += "or (ex.studyId = ?" + i + " and ex.centerId = ?" + (i + 1) + ") ";
			i += 2;
		}
		queryEndStr += ")";
		
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
		
		Query query = entityManager.createQuery(queryStr);
		
		query.setParameter(1, studyIds);
		if (preclinical != null) {
			query.setParameter(2, preclinical);
		}
		i = 2;
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
				query.setParameter(2, preclinical);
				queryCount.setParameter(2, preclinical);
			}
			for (Pair<Long, Long> studyCenter : studyCenterIds) {
				queryCount.setParameter(i, studyCenter.getFirst());
				queryCount.setParameter(i + 1, studyCenter.getSecond());
				i += 2;
			}
			total = (Long) queryCount.getSingleResult();
			query.setFirstResult(Math.toIntExact(pageable.getPageNumber() * pageable.getPageSize()));
			query.setMaxResults(pageable.getPageSize());
		}

		return new Pair<>(query.getResultList(), total);
	}
	
}
