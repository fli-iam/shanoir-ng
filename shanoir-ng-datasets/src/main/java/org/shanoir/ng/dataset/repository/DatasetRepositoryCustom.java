package org.shanoir.ng.dataset.repository;

import java.util.List;

import org.shanoir.ng.dataset.dto.StudyStatisticsDTO;

public interface DatasetRepositoryCustom {

	public List<Object[]> queryStatistics(String studyNameInRegExp, String studyNameOutRegExp,
			String subjectNameInRegExp, String subjectNameOutRegExp) throws Exception;

	public List<StudyStatisticsDTO> queryManageStudyStatistics(Long studyId) throws Exception;

}
