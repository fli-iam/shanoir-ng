package org.shanoir.ng.shared.common;

/**
 * Shared service.
 * 
 * @author ifakhfakh
 *
 */
public interface CommonService {
	
	/**
	 * Find study name, subject name and center name by their ids.
	 * 
	 * @param commonIdsDTO
	 * 			DTO with ids.
	 * @return corresponding data.
	 */
	CommonIdNamesDTO findByIds(CommonIdsDTO commonIdsDTO);

}
