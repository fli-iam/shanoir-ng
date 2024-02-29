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

			studyId = (Long) row[0];
			Long centerId = (Long) row[1];
			String centerName = (String) row[2];
			String centerPrefix = (String) row[3];
			Long subjectId = (Long) row[4];
			String commonName = (String) row[5];
			Long examinationId = (Long) row[6];
			Date examinationDate = (Date) row[7];
			Long datasetAcquisitionId = (Long) row[8];
			Date importDate = (Date) row[9];
			Long datasetId = (Long) row[10];
			String modality = (String) row[11];
			String quality = (String) row[12];

			StudyStatisticsDTO dto = new StudyStatisticsDTO();

			dto.setStudyId(studyId);
			dto.setCenterId(centerId);
			dto.setCenterName(centerName);
			dto.setCenterPrefix(centerPrefix);
			dto.setSubjectId(subjectId);
			dto.setCommonName(commonName);
			dto.setExaminationId(examinationId);
			dto.setExaminationDate(examinationDate);
			dto.setDatasetAcquisitionId(datasetAcquisitionId);
			dto.setImportDate(importDate);
			dto.setDatasetId(datasetId);
			dto.setModality(modality);
			dto.setQuality(quality);

			studyStatisticsList.add(dto);

		}

		return studyStatisticsList;
	}

}
