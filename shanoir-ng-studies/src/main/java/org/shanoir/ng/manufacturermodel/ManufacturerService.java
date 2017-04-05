package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudyException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Manufacturer service.
 * 
 * @author msimon
 *
 */
public interface ManufacturerService extends UniqueCheckableService<Manufacturer> {

	/**
	 * Get all the manufacturers.
	 * 
	 * @return a list of manufacturers.
	 */
	List<Manufacturer> findAll();

	/**
	 * Find manufacturer by its id.
	 *
	 * @param id
	 *            manufacturer id.
	 * @return a manufacturer or null.
	 */
	Manufacturer findById(Long id);

	/**
	 * Save an manufacturer.
	 *
	 * @param manufacturer
	 *            manufacturer to create.
	 * @return created manufacturer.
	 * @throws ShanoirStudyException
	 */
	Manufacturer save(Manufacturer manufacturer) throws ShanoirStudyException;

}
