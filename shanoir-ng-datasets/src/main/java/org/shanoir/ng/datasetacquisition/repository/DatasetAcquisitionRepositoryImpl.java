package org.shanoir.ng.datasetacquisition.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.mysql.cj.conf.ConnectionUrlParser.Pair;

@Component
public class DatasetAcquisitionRepositoryImpl implements DatasetAcquisitionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


	@SuppressWarnings("unchecked")
	@Override
	public Page<DatasetAcquisition> findByExaminationByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds,
			Iterable<Long> studyIds, Pageable pageable) {
		
		String queryEndStr = "from DatasetAcquisition as da "
				+ "join da.examination as ex "
				+ "where ex.studyId in ?1 ";
		int i = 2;
		for (Pair<Long, Long> studyCenter : studyCenterIds) {
			queryEndStr += "or (ex.studyId = ?" + i + " and ex.centerId = ?" + (i + 1) + ") ";
			i += 2;
		}
		
		String queryStr = "select da " + queryEndStr;
		String queryCountStr = "select count(da) " + queryEndStr;
		
		if (pageable.getSort() != null && pageable.getSort().isSorted()) {
			queryStr += "order by ";
			int isort = 0;
			for (Sort.Order order : pageable.getSort()) {
				if (isort >= 1) {
					queryStr += ",";
				}
				queryStr += order.getProperty();
				queryStr += " " + order.getDirection() + " ";
				isort ++;
			}
		}
		
		Query query = entityManager.createQuery(queryStr);
		Query queryCount = entityManager.createQuery(queryCountStr);
		
		query.setParameter(1, studyIds);
		queryCount.setParameter(1, studyIds);
		i = 2;
		for (Pair<Long, Long> studyCenter : studyCenterIds) {
			query.setParameter(i, studyCenter.left);
			query.setParameter(i + 1, studyCenter.right);
			queryCount.setParameter(i, studyCenter.left);
			queryCount.setParameter(i + 1, studyCenter.right);
			i += 2;
		}
	
		Long total = (Long) queryCount.getSingleResult();

		query.setFirstResult(Math.toIntExact(pageable.getOffset() + (pageable.getPageNumber() * pageable.getPageSize())));
		query.setMaxResults(pageable.getPageSize());

		return new PageImpl<DatasetAcquisition>(query.getResultList(), pageable, total);
	}

}
