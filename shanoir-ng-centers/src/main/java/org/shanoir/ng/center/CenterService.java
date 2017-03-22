package org.shanoir.ng.center;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirCenterException;
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
	 * @throws ShanoircenterException
	 */
	void deleteById(Long id) throws ShanoirCenterException;

	/**
	 * Get all the center.
	 * 
	 * @return a list of centers.
	 */
	List<Center> findAll();

	/**
	 * Find center by data.
	 *
	 * @param data
	 *            data.
	 * @return a center.
	 */
	Optional<Center> findByData(String data);

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
	 * @throws ShanoircenterException
	 */
	Center save(Center center) throws ShanoirCenterException;

	/**
	 * Update a center.
	 *
	 * @param center
	 *            center to update.
	 * @return updated center.
	 * @throws ShanoircenterException
	 */
	Center update(Center center) throws ShanoirCenterException;

	/**
	 * Update a center from the old Shanoir
	 * 
	 * @param center
	 *            center.
	 * @throws ShanoircenterException
	 */
	void updateFromShanoirOld(Center center) throws ShanoirCenterException;

}
