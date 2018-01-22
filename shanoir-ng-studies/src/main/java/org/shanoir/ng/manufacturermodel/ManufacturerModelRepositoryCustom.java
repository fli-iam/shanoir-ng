package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.model.ItemRepositoryCustom;

/**
 * Custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelRepositoryCustom extends ItemRepositoryCustom<ManufacturerModel> {

	/**
	 * Find id and name for all Manufacturer Models.
	 * 
	 * @return list of Manufacturer Models.
	 */
	List<IdNameDTO> findIdsAndNames();

}
