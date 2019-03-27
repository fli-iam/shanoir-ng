package org.shanoir.ng.center.service;

import java.util.List;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Center service.
 *
 * @author msimon
 * @author jlouis
 */
public interface CenterService extends BasicEntityService<Center> {

	/**
	 * Delete a center.
	 * 
	 * @param id center id.
	 * @throws EntityNotFoundException when the id could not be found in the database.
	 * @throws UndeletableDependenciesException if the center has dependencies that cannot be deleted.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	void deleteByIdCheckDependencies(Long id) throws EntityNotFoundException, UndeletableDependenciesException;

	/**
	 * Find center by name.
	 *
	 * @param name center name.
	 * @return a center.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Center findByName(String name);


	/**
	 * Find id and name for all centers.
	 * 
	 * @return list of centers.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdNameDTO> findIdsAndNames();

}
