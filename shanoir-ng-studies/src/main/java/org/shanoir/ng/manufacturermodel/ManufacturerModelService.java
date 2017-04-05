package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudyException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Manufacturer model service.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelService extends UniqueCheckableService<ManufacturerModel> {

	/**
	 * Get all the manufacturer models.
	 * 
	 * @return a list of manufacturer models.
	 */
	List<ManufacturerModel> findAll();

	/**
	 * Find manufacturer model by its id.
	 *
	 * @param id
	 *            manufacturer model id.
	 * @return a manufacturer model or null.
	 */
	ManufacturerModel findById(Long id);

	/**
	 * Save an manufacturer model.
	 *
	 * @param manufacturerModel
	 *            manufacturer model to create.
	 * @return created manufacturer model.
	 * @throws ShanoirStudyException
	 */
	ManufacturerModel save(ManufacturerModel manufacturerModel) throws ShanoirStudyException;

}
