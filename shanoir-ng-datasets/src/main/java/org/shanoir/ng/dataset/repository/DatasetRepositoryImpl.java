package org.shanoir.ng.dataset.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Component;

import org.shanoir.ng.dataset.dto.StudyStatisticsDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatasetRepositoryImpl implements DatasetRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> queryStatistics(String studyNameInRegExp, String studyNameOutRegExp,
            String subjectNameInRegExp, String subjectNameOutRegExp) throws Exception {
        
		//"getStatistics" is the name of the MySQL procedure
		StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getStatistics"); 

		//Declare the parameters in the same order
		query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

		//Pass the parameter values
		query.setParameter(1, studyNameInRegExp);
		query.setParameter(2, studyNameOutRegExp);
		query.setParameter(3, subjectNameInRegExp);
		query.setParameter(4, subjectNameOutRegExp);

		//Execute query
		query.execute();
		
		List<Object[]> results = query.getResultList();
		return results;
    }

	@Override
	public List<StudyStatisticsDTO> queryManageStudyStatistics(Long studyId) throws Exception {

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
			String centerNumber = (String) row[2];
			Long subjectId = (Long) row[3];
			String commonName = (String) row[4];
			Long examinationId = (Long) row[5];
			LocalDate examinationDate = (LocalDate) row[6];
			Long datasetAcquisitionId = (Long) row[7];
			LocalDate importDate = (LocalDate) row[8];
			Long datasetId = (Long) row[9];
			String modality = (String) row[10];
			String quality = (String) row[11];

			StudyStatisticsDTO dto = new StudyStatisticsDTO(studyId, centerName, centerNumber, subjectId, commonName, examinationId, examinationDate, datasetAcquisitionId, importDate, datasetId, modality, quality);

			dto.setStudyId(studyId);
			dto.setCenterName(centerName);
			dto.setCenterNumber(centerNumber);
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
