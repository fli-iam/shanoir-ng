package org.shanoir.ng.center.repository;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface CenterRepositoryCustom {


	List<IdNameDTO> findIdsAndNames();
	
}
