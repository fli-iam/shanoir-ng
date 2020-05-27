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

package org.shanoir.ng.dataset.service;

import java.util.List;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Dataset service.
 *
 * @author msimon
 *
 */
public interface DatasetService {

	/**
	 * Delete a dataset.
	 * 
	 * @param id dataset id.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#id, 'CAN_ADMINISTRATE'))")
	void deleteById(Long id) throws EntityNotFoundException;

	/**
	 * Find dataset by its id.
	 *
	 * @param id dataset id.
	 * @return a dataset or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnTrustedDataset(returnObject, 'CAN_SEE_ALL')")
	Dataset findById(Long id);

	/**
	 * Find datasets by their ids.
	 *
	 * @param ids datasets ids.
	 * @return a list if datasets or an empty list.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.hasRightOnTrustedDataset(returnObject, 'CAN_SEE_ALL')")
	List<Dataset> findByIdIn(List<Long> id);

	/**
	 * Save a dataset.
	 *
	 * @param dataset dataset to create.
	 * @return created dataset.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#dataset.getStudyId(), 'CAN_IMPORT'))")
	Dataset create(Dataset dataset);

	/**
	 * Update a dataset.
	 *
	 * @param dataset dataset to update.
	 * @return updated dataset.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasUpdateRightOnDataset(#dataset, 'CAN_ADMINISTRATE'))")
	Dataset update(Dataset dataset) throws EntityNotFoundException;
	
	/**
	 * Find every dataset
	 * 
	 * @return datasets
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetList(returnObject, 'CAN_SEE_ALL')")
	List<Dataset> findAll();

	/**
	 * Fetch the asked page
	 * 
	 * @return datasets
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.checkDatasetPage(returnObject, 'CAN_SEE_ALL')")
	public Page<Dataset> findPage(final Pageable pageable);


	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.checkDatasetPage(returnObject, 'CAN_SEE_ALL')")
	public List<Dataset> findByStudyId(Long studyId);

}
