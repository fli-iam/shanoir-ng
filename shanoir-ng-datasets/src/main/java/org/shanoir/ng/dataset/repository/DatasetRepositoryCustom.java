package org.shanoir.ng.dataset.repository;

import java.util.List;

public interface DatasetRepositoryCustom {

	public List<Object[]> queryStatistics(String studyNameInRegExp, String studyNameOutRegExp,
			String subjectNameInRegExp, String subjectNameOutRegExp) throws Exception;

	public byte[] queryManageStudyStatistics(Long studyId) throws Exception;

}
