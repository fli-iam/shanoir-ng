package org.shanoir.ng.study.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Component;

import org.shanoir.ng.study.dto.StudyStatisticsDTO;

import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

@Component
public class StudyRepositoryImpl implements StudyRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

	@Override
	public List<StudyStatisticsDTO> queryStudyStatistics(Long studyId) throws Exception {

		//"getStudyStatistics" is the name of the MySQL procedure
		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getStudyStatistics"); 

		//Declare the parameters in the same order
		query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);

		//Pass the parameter values
		query.setParameter(1, studyId);

		//Execute query
		query.execute();
		
		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		List<StudyStatisticsDTO> studyStatisticsList = new ArrayList<>();

		for (Object[] row : results) {

			StudyStatisticsDTO dto = new StudyStatisticsDTO();

			dto.setStudyId((Long) row[0]);
			dto.setCenterId((Long) row[1]);
			dto.setCenterName(String.valueOf(row[2]));
			dto.setCenterPrefix(String.valueOf(row[3]));
			dto.setSubjectId((Long) row[4]);
			dto.setCommonName(String.valueOf(row[5]));
			dto.setExaminationId((Long) row[6]);
			dto.setExaminationDate((Date) row[7]);
			dto.setDatasetAcquisitionId((Long) row[8]);
			dto.setImportDate((Date) row[9]);
			dto.setDatasetId((Long) row[10]);
			dto.setModality(String.valueOf(row[11]));
			dto.setQuality(String.valueOf(row[12]));

			studyStatisticsList.add(dto);

		}

		return studyStatisticsList;
	}

}
