package org.shanoir.ng.center;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirStudyException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * center service.
 *
 * @author msimon
 *
 */
public interface CenterService extends UniqueCheckableService<Center> {

	/**
	 * Delete a center.
	 * 
	 * @param id
	 *            center id.
	 * @throws ShanoirStudyException
	 */
	void deleteById(Long id) throws ShanoirStudyException;

	/**
	 * Get all the center.
	 * 
	 * @return a list of centers.
	 */
	List<Center> findAll();

	/**
	 * Find center by name.
	 *
	 * @param name
	 *            name.
	 * @return a center.
	 */
	Optional<Center> findByName(String name);

	/**
	 * Find center by its id.
	 *
	 * @param id
	 *            center id.
	 * @return a center or null.
	 */
	Center findById(Long id);

	/**
	 * Save a center.
	 *
	 * @param center
	 *            center to create.
	 * @return created center.
	 * @throws ShanoirStudyException
	 */
	Center save(Center center) throws ShanoirStudyException;

	/**
	 * Update a center.
	 *
	 * @param center
	 *            center to update.
	 * @return updated center.
	 * @throws ShanoirStudyException
	 */
	Center update(Center center) throws ShanoirStudyException;

	/**
	 * Update a center from the old Shanoir
	 * 
	 * @param center
	 *            center.
	 * @throws ShanoirStudyException
	 */
	void updateFromShanoirOld(Center center) throws ShanoirStudyException;

}
