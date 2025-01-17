package org.shanoir.ng.dataset.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.shanoir.ng.dataset.service.CreateStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatasetRepositoryImpl implements DatasetRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

	private static final Logger LOG = LoggerFactory.getLogger(DatasetRepositoryImpl.class);
    @SuppressWarnings("unchecked")
	@Override
    public List<Object[]> queryStatistics(String studyNameInRegExp, String studyNameOutRegExp,
            String subjectNameInRegExp, String subjectNameOutRegExp) throws Exception {

		int startRow = 0;
		int blocSize = 200000;

		List<Object[]> allResults = new ArrayList<>();

		while (true) {
			LOG.error("======");
			LOG.error("startRow : " + startRow);
			LOG.error("blocSize : " + blocSize);
			//"getStatistics" is the name of the MySQL procedure
			StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getStatistics");

			//Declare the parameters in the same order
			query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter(5, Integer.class, ParameterMode.IN);
			query.registerStoredProcedureParameter(6, Integer.class, ParameterMode.IN);

			//Pass the parameter values
			query.setParameter(1, studyNameInRegExp);
			query.setParameter(2, studyNameOutRegExp);
			query.setParameter(3, subjectNameInRegExp);
			query.setParameter(4, subjectNameOutRegExp);
			query.setParameter(5, startRow);
			query.setParameter(6, blocSize);

			//Execute query
			@SuppressWarnings("unchecked")
			List<Object[]> results = query.getResultList();
			LOG.error("result size : " + results.size());

			if (results.isEmpty()) {
				LOG.error("break");
				break;
			}
			allResults.addAll(results);
			startRow += blocSize;
		}

		return allResults;
    }
}
