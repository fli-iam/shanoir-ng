/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.events;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.shared.paging.PageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
@Component
public class ShanoirEventRepositoryImpl implements ShanoirEventRepositoryCustom {


    private static final Logger LOG = LoggerFactory.getLogger(ShanoirEventRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ShanoirEvent> findByStudyIdOrderByCreationDateDescAndSearch(Pageable pageable, Long studyId, String searchStr, String searchField) {
        Pair<List<ShanoirEvent>, Long> pair = find(studyId, pageable, searchStr, searchField);
        return new PageImpl<ShanoirEvent>(pair.getFirst(), pageable, pair.getSecond());
    }

    private Pair<List<ShanoirEvent>, Long> find(Long studyId, Pageable pageable, String searchStr, String searchField) {

        String queryEndStr = "from ShanoirEvent as e ";
        int nbPreParams = 1;
        int searchStrIndex = -1;

        if (!StringUtils.isEmpty(searchStr) || pageable.getSort().toString().indexOf("username") != -1) {
            queryEndStr += "inner join User as u on e.userId = u.id ";
        }

        queryEndStr += "where ";

        if (!StringUtils.isEmpty(searchStr)) {
            nbPreParams++;
            searchStrIndex = nbPreParams;
            if (searchField != null && !searchField.isEmpty()) {
                if (searchField.equals("objectId")) {
                    queryEndStr += "CAST(e.objectId as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                } else if (searchField.equals("userId") || searchField.equals("username")) {
                    queryEndStr += "u.username LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                } else if (searchField.equals("eventType")) {
                    queryEndStr += "e.eventType LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                } else if (searchField.equals("message")) {
                    queryEndStr += "e.message LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                } else if (searchField.equals("creationDate")) {
                    queryEndStr += "CAST(DATE_FORMAT(e." + searchField + ", '%d/%m/%Y') as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                } else {
                    queryEndStr += "e." + searchField + " LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                }
            } else {
                // filter '*'
                queryEndStr += "(CAST(e.objectId as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                queryEndStr += "or u.username LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                queryEndStr += "or e.eventType LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                queryEndStr += "or CAST(DATE_FORMAT(e.creationDate, '%d/%m/%Y') as String) LIKE CONCAT('%', ?" + searchStrIndex + ", '%') ";
                queryEndStr += "or e.message LIKE CONCAT('%', ?" + searchStrIndex + ", '%')) ";
            }
            queryEndStr += " and ";
        }
        queryEndStr += " e.studyId = ?1";

        String queryStr = "select e " + queryEndStr;

        if (pageable != null && pageable.getSort() != null && pageable.getSort().isSorted()) {
            queryStr += " order by ";
            int isort = 0;
            for (Sort.Order order : pageable.getSort()) {
                if (isort >= 1) {
                    queryStr += ",";
                }
                if ("username".equals(order.getProperty())) {
                    queryStr += "u." + order.getProperty();
                } else {
                    queryStr += "e." + order.getProperty();
                }
                queryStr += " " + order.getDirection() + " ";
                isort++;
            }
            queryStr += ", e.creationDate desc";
        }
        if (pageable == null || "unsorted".equals(pageable.getSort().toString().toLowerCase())) {
            queryStr += " order by e.creationDate desc ";
        }

        Query query = entityManager.createQuery(queryStr);

        query.setParameter(1, studyId);
        if (!StringUtils.isEmpty(searchStr)) {
            query.setParameter(searchStrIndex, searchStr);
        }

        Long total = null;
        if (pageable != null) {
            String queryCountStr = "select count(e) " + queryEndStr;
            Query queryCount = entityManager.createQuery(queryCountStr);
            queryCount.setParameter(1, studyId);
            if (!StringUtils.isEmpty(searchStr)) {
                queryCount.setParameter(searchStrIndex, searchStr);
            }
            total = (Long) queryCount.getSingleResult();
            query.setFirstResult(Math.toIntExact(pageable.getPageNumber() * pageable.getPageSize()));
            query.setMaxResults(pageable.getPageSize());
        }

        return Pair.of(query.getResultList(), total);
    }

    public Long countByLastUpdateAfter(Date expiryDate) {
        String queryStr = "select count(e) from ShanoirEvent as e where e.lastUpdate > ?1";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(1, expiryDate);
        return (Long) query.getSingleResult();
    }

}
