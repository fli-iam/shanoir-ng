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

		//"getManageStudyStatistics" is the name of the MySQL procedure
		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getManageStudyStatistics"); 

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
			String centerName = (String) row[1];
			Long subjectId = (Long) row[2];
			String commonName = (String) row[3];
			Long examinationId = (Long) row[4];
			Date examinationDate = (Date) row[5];
			Long datasetAcquisitionId = (Long) row[6];
			Date importDate = (Date) row[7];
			Long datasetId = (Long) row[8];
			String modality = (String) row[9];
			String quality = (String) row[10];

			StudyStatisticsDTO dto = new StudyStatisticsDTO();

			dto.setStudyId(studyId);
			dto.setCenterName(centerName);
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
