package org.shanoir.ng.manufacturermodel.repository;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;

/**
 * Custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelRepositoryCustom {

	/**
	 * Find id and name for all Manufacturer Models.
	 * 
	 * @return list of Manufacturer Models.
	 */
	List<IdName> findIdsAndNames();
	
	
	/**
	 * Find id and name for Manufacturer Models related to a center.
	 * 
	 * @param centerId: the id of the center
	 * 
	 * @return list of Manufacturer Models.
	 */
	List<IdName> findIdsAndNamesForCenter(Long centerId);
}
