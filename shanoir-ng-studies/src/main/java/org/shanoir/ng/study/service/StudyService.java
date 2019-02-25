package org.shanoir.ng.study.service;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.AccessDeniedException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;
import org.shanoir.ng.study.model.Study;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Study service.
 *
 */
@Service
public interface StudyService extends UniqueCheckableService<Study> {


	/**
	 * Delete a study. Do check before if current user can delete study!
	 *
	 * @param id
	 *            study id.
	 * @throws EntityNotFoundException 
	 * @throws AccessDeniedException 
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and hasPermission(#id, 'Study', 'CAN_ADMINISTRATE')")
	void deleteById(Long id) throws EntityNotFoundException, AccessDeniedException;


	/**
	 * Find study by its id. Check if current user can see study.
	 *
	 * @param id
	 *            study id.
	 * @return a study or null.
	 * @throws AccessDeniedException 
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	@PostAuthorize("hasPermission(returnObject, 'CAN_SEE_ALL')")
	Study findById(Long id); 


	/**
	 * Get all the studies
	 * 
	 * @return a list of studies
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	List<Study> findAll();

	
	/**
	 * Find id and name for all studies.
	 * 
	 * @return list of studies.
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'EXPERT')")
	List<IdNameDTO> findIdsAndNames();


	/**
	 * add new study
	 * 
	 * @param study
	 * @return created Study
	 * @throws ShanoirStudiesException
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and hasPermission(#id, 'Study', 'CAN_ADMINISTRATE')")
	Study save(Study study);

	
	/**
	 * Update a study
	 * 
	 * @param study
	 * @return updated study
	 * @throws ShanoirStudiesException
	 * @throws EntityNotFoundException 
	 * @throws AccessDeniedException 
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and hasPermission(#id, 'Study', 'CAN_ADMINISTRATE')")
	Study update(Study study) throws EntityNotFoundException, AccessDeniedException;

}
