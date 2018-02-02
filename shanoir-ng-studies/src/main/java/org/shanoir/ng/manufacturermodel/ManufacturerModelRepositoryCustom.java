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
	
	
	/**
	 * Find id and name for Manufacturer Models related to a center.
	 * 
	 * @param centerId: the id of the center
	 * 
	 * @return list of Manufacturer Models.
	 */
	List<IdNameDTO> findIdsAndNamesForCenter(Long centerId);
}
