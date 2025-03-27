package org.shanoir.ng.datasetacquisition.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatasetAcquisitionRepositoryImpl implements DatasetAcquisitionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @SuppressWarnings("unchecked")
    public Page<DatasetAcquisition> findPageByStudyCenterOrStudyIdIn(Iterable<Pair<Long, Long>> studyCenterIds,
            Iterable<Long> studyIds, Pageable pageable) {
        
        String queryEndStr = "from DatasetAcquisition as da "
                + "join da.examination as ex "
                + "where ex.study.id in ?1 ";
        int i = 2;
        for (Pair<Long, Long> studyCenter : studyCenterIds) {
            queryEndStr += "or (ex.study.id = ?" + i + " and ex.centerId = ?" + (i + 1) + ") ";
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
                queryStr += "da." + order.getProperty();
                queryStr += " " + order.getDirection() + " ";
                isort++;
            }
        }
        
        Query query = entityManager.createQuery(queryStr);
        Query queryCount = entityManager.createQuery(queryCountStr);
        
        query.setParameter(1, studyIds);
        queryCount.setParameter(1, studyIds);
        i = 2;
        for (Pair<Long, Long> studyCenter : studyCenterIds) {
            query.setParameter(i, studyCenter.getFirst());
            query.setParameter(i + 1, studyCenter.getSecond());
            queryCount.setParameter(i, studyCenter.getFirst());
            queryCount.setParameter(i + 1, studyCenter.getSecond());
            i += 2;
        }
    
        Long total = (Long) queryCount.getSingleResult();

        query.setFirstResult(Math.toIntExact(pageable.getPageNumber() * pageable.getPageSize()));
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<DatasetAcquisition>(query.getResultList(), pageable, total);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DatasetAcquisition> findByStudyCardIdAndStudyCenterOrStudyIdIn(Long studyCardId,
            Iterable<Pair<Long, Long>> studyCenterIds, Iterable<Long> studyIds) {
        
        String queryEndStr = "from DatasetAcquisition as da "
                + "join da.examination as ex "
                + "where (ex.study.id in ?1 ";
        int i = 2;
        for (Pair<Long, Long> studyCenter : studyCenterIds) {
            queryEndStr += "or (ex.study.id = ?" + i + " and ex.centerId = ?" + (i + 1) + ") ";
            i += 2;
        }
        queryEndStr += ") and da.studyCard.id = ?" + i;
        i++;
        
        String queryStr = "select da " + queryEndStr;
        
        Query query = entityManager.createQuery(queryStr);
        
        query.setParameter(1, studyIds);
        i = 2;
        for (Pair<Long, Long> studyCenter : studyCenterIds) {
            query.setParameter(i, studyCenter.getFirst());
            query.setParameter(i + 1, studyCenter.getSecond());
            i += 2;
        }
        query.setParameter(i, studyCardId);

        return query.getResultList();
    }
}
