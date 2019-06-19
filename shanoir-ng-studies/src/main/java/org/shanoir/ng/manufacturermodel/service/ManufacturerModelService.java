package org.shanoir.ng.manufacturermodel.service;

import java.util.List;

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Manufacturer model service.
 * 
 * @author jlouis
 *
 */
public interface ManufacturerModelService extends BasicEntityService<ManufacturerModel> {

	
	/**
	 * Find id and name for all manufacturer models.
	 * 
	 * @return list of IdNameDTO.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdName> findIdsAndNames();
	
	/**
	 * Find id and name for manufacturer models related to a center.
	 * 
	 * @param centerId: the id of the center
	 * @return list of IdNameDTO.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdName> findIdsAndNamesForCenter(Long centerId);

}
