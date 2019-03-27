package org.shanoir.ng.center.repository;

import java.util.List;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.repository.CustomRepository;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface CenterRepositoryCustom extends CustomRepository<Center> {


	List<IdNameDTO> findIdsAndNames();
	
}
