package org.shanoir.ng.shared.api;

import org.shanoir.ng.study.dto.StudySubjectCenterNamesDTO;

/**
 * Study shared service.
 * 
 * @author ifakhfakh
 *
 */
public interface StudyServiceShared {
	
	/**
	 * Find study name, subject name and center name by their ids.
	 * 
	 * @param studyId
	 * 			study id
	 * @param subjectId
	 * 			subject id
	 * @param centerId
	 * 			center id
	 * @return StudySubjectCenterNamesDTO or null.
	 */
	StudySubjectCenterNamesDTO findByIds(Long studyId, Long subjectId,Long centerId);

}
