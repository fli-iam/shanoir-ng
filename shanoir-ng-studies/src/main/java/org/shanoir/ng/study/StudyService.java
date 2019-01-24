package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Study service.
 * 
 * @author msimon
 *
 */
public interface StudyService extends UniqueCheckableService<Study> {

	/**
	 * Check if an user can update a study.
	 * 
	 * @param studyId
	 *            study id.
	 * @param userId
	 *            user id.
	 * @return true or false.
	 */
	boolean canUserUpdateStudy(Long studyId, Long userId);

	/**
	 * Delete a study. Do check before if current user can delete study!
	 *
	 * @param id
	 *            study id.
	 */
	void deleteById(Long id);

	/**
	 * Get all the studies
	 * 
	 * @return a list of studies
	 */
	List<Study> findAll();

	/**
	 * Find study by its id.
	 *
	 * @param id
	 *            study id.
	 * @return a study or null.
	 */
	Study findById(Long id);

	/**
	 * Find study by its id. Check if current user can see study.
	 *
	 * @param id
	 *            study id.
	 * @param userId
	 *            user id.
	 * @return a study or null.
	 * @throws ShanoirStudiesException
	 */
	Study findById(Long id, Long userId) throws ShanoirStudiesException;

	/**
	 * Find id and name for all studies.
	 * 
	 * @return list of studies.
	 */
	List<IdNameDTO> findIdsAndNames();

	/**
	 * Find all studies for a user.
	 * 
	 * @param userId
	 *            user id.
	 * @return a list of studies.
	 */
	List<Study> findStudiesByUserId(Long userId);

	/**
	 * Find all studies for a user with permission level (studyUserType) lower than or equal to specified value.
	 * 
	 * @param userId
	 *            user id.
	 * @param studyUserTypeId
	 *            studyUserType id.
	 * @return a list of studies.
	 */
	List<Study> findStudiesByUserIdAndStudyUserTypeLessThanEqual(final Long userId,final Integer studyUserTypeId);

	/**
	 * Find all studies that the user is allowed to see and to import.
	 * 
	 * @param userId
	 *            user id.
	 * @return a list of studies.
	 * @throws ShanoirException
	 */
	List<Study> findStudiesByUserIdAndStudyUserType(Long userId);
	
	/**
	 * Check if an user is responsible of the study.
	 * 
	 * @param studyId
	 *            study id.
	 * @param userId
	 *            user id.
	 * @return true if user is responsible
	 * @throws ShanoirStudiesException
	 */
	boolean isUserResponsible(Long studyId, Long userId) throws ShanoirStudiesException;

	/**
	 * add new study
	 * 
	 * @param study
	 * @return created Study
	 * @throws ShanoirStudiesException
	 */
	Study save(Study study) throws ShanoirStudiesException;

	/**
	 * Update a study
	 * 
	 * @param study
	 * @return updated study
	 * @throws ShanoirStudiesException
	 */
	Study update(Study study) throws ShanoirStudiesException;

	/**
	 * Update a Study from the old Shanoir
	 *
	 * @param Study
	 *            Study.
	 * @throws ShanoirStudiesException
	 */
	void updateFromShanoirOld(Study study) throws ShanoirStudiesException;
}
