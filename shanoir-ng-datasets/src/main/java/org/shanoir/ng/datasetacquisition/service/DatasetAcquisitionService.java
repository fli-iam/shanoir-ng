/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.datasetacquisition.service;

import java.util.List;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DatasetAcquisitionService {
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or returnObject == null or @datasetSecurityService.hasRightOnStudy(returnObject.getExamination().getStudyId(), 'CAN_SEE_ALL')")
	DatasetAcquisition findById(Long id);

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetAcquisitionList(returnObject, 'CAN_SEE_ALL')")
	List<DatasetAcquisition> findByStudyCard(Long id);
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.checkDatasetAcquisitionPage(returnObject, 'CAN_SEE_ALL')")
	public Page<DatasetAcquisition> findPage(final Pageable pageable);

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and #entity.getId() == null and @datasetSecurityService.hasRightOnStudy(#entity.getExamination().getStudyId(), 'CAN_IMPORT')")
	DatasetAcquisition create(DatasetAcquisition entity);

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasUpdateRightOnDatasetAcquisition(#entity, 'CAN_ADMINISTRATE')")
	DatasetAcquisition update(DatasetAcquisition entity) throws EntityNotFoundException;
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and  @datasetSecurityService.hasRightOnDatasetAcquisition(#id, 'CAN_ADMINISTRATE')")
	void deleteById(Long id) throws EntityNotFoundException;

}
