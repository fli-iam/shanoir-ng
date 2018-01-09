package org.shanoir.ng.center;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.model.ItemRepositoryCustom;

/**
 * Custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface CenterRepositoryCustom extends ItemRepositoryCustom<Center> {

	/**
	 * Find id and name for all centers.
	 * 
	 * @return list of centers.
	 */
	List<IdNameDTO> findIdsAndNames();

}
