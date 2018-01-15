package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;

/**
 * Manufacturer model service.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelService {

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
	 * @throws ShanoirStudiesException
	 */
	ManufacturerModel save(ManufacturerModel manufacturerModel) throws ShanoirStudiesException;

	/**
	 * Update a manufacturer model.
	 *
	 * @param manufacturerModel
	 *            manufacturer model to update.
	 * @return updated manufacturer model.
	 * @throws ShanoirStudiesException
	 */
	ManufacturerModel update(ManufacturerModel manufacturerModel) throws ShanoirStudiesException;
	
	/**
	 * Find id and name for all manufacturer models.
	 * 
	 * @return list of IdNameDTO.
	 */
	List<IdNameDTO> findIdsAndNames();

}
