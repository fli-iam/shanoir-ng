package org.shanoir.ng.examination.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.paging.PageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
public class ExaminationRepositoryImpl implements ExaminationRepositoryCustom {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;


	@Override
	public Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, Pageable pageable, Boolean preclinical) {
		
		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, pageable, preclinical, null, null, null);
		return new PageImpl<Examination>(pair.getFirst(), pageable, pair.getSecond());
	}
	
	@Override
	public Page<Examination> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, Pageable pageable) {
		
		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, pageable, null, null, null, null);
		return new PageImpl<Examination>(pair.getFirst(), pageable, pair.getSecond());
	}

	@Override
	public Page<Examination> findPageByStudyCenterOrStudyIdInAndSubjectName(Iterable<Pair<Long, Long>> studyCenterIds,
																			Iterable<Long> studyIds, String subjectName, Pageable pageable) {

		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, pageable, null, subjectName, null, null);
		return new PageImpl<Examination>(pair.getFirst(), pageable, pair.getSecond());
	}
	@Override
	public Page<Examination> findPageByStudyCenterOrStudyIdInAndSearch(Iterable<Pair<Long, Long>> studyCenterIds,
																			Iterable<Long> studyIds, Pageable pageable, Boolean preclinical, String searchStr, String searchField) {

		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, pageable, preclinical, null, searchStr, searchField);
		return new PageImpl<Examination>(pair.getFirst(), pageable, pair.getSecond());
	}

	@Override
	public List<Examination> findAllByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds) {
		
		Pair<List<Examination>, Long> pair = find(studyCenterIds, studyIds, null, null, null, null, null);
		return pair.getFirst();
	}
	
	@SuppressWarnings("unchecked")
	private Pair<List<Examination>, Long> find(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, Pageable pageable, Boolean preclinical, String subjectName, String searchStr, String searchField) {

		String queryEndStr = "from Examination as ex ";
		int nbPreParams = 1;
		int preclinicalIndex = -1;
		int subjectNameIndex = -1;
		int searchStrIndex = -1;

		if (StringUtils.isEmpty(searchField) || searchField.equals("center.name")) {
			queryEndStr += "inner join Center as c on ex.centerId = c.id ";
		}
		if (preclinical != null) {
			nbPreParams++;
			preclinicalIndex = nbPreParams;
			queryEndStr +=  "where ex.preclinical = ?" + preclinicalIndex + " ";
		}
		if (subjectName != null) {
			nbPreParams++;
			subjectNameIndex = nbPreParams;
			queryEndStr +=  "and ex.subject.name LIKE ?" + subjectNameIndex + " ";
		}
		if (!StringUtils.isEmpty(searchStr)) {
			nbPreParams++;
			searchStrIndex = nbPreParams;
			if (searchField != null && !searchField.isEmpty()) {
				if (searchField.equals("id")) {
					queryEndStr += "and CAST(ex.id as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				} else if (searchField.equals("center.name")) {
					queryEndStr += "and c.name LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				} else if (searchField.equals("examinationDate")) {
					queryEndStr += "and CAST(DATE_FORMAT(ex." + searchField + ", '%d/%m/%Y') as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				} else {
					queryEndStr += "and ex." + searchField + " LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				}
			} else {
				// filter '*'
				queryEndStr += "and (CAST(ex.id as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				queryEndStr += "or c.name LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				queryEndStr += "or CAST(DATE_FORMAT(ex.examinationDate, '%d/%m/%Y') as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				queryEndStr += "or ex.study.name LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				queryEndStr += "or ex.subject.name LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
				queryEndStr += "or ex.comment LIKE CONCAT('%', ?" + searchStrIndex + ", '%')) ";
			}
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

		Query query = entityManager.createQuery(queryStr);

		query.setParameter(1, studyIds);
		if (preclinical != null) {
			query.setParameter(preclinicalIndex, preclinical);
		}
		if (subjectName != null) {
			query.setParameter(subjectNameIndex, subjectName);
		}
		if (searchStr != null) {
			query.setParameter(searchStrIndex, searchStr);
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
			if (searchStr != null) {
				queryCount.setParameter(searchStrIndex, searchStr);
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

		return Pair.of(query.getResultList(), total);
	}
	
}
