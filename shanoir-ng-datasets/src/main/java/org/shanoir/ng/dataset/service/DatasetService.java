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

import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.VolumeByFormatDTO;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
	 * @throws ShanoirException 
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#id, 'CAN_ADMINISTRATE'))")
	void deleteById(Long id) throws EntityNotFoundException, ShanoirException, SolrServerException, IOException, RestServiceException;
	
	/**
	 * Delete several datasets.
	 * 
	 * @param ids dataset ids.
	 * @throws EntityNotFoundException
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnEveryDataset(#ids, 'CAN_ADMINISTRATE'))")
	void deleteByIdIn(List<Long> ids) throws EntityNotFoundException, SolrServerException, IOException, RestServiceException;

	/**
	 * Find dataset by its id.
	 *
	 * @param id dataset id.
	 * @return a dataset or null.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or returnObject == null or @datasetSecurityService.hasRightOnTrustedDataset(returnObject, 'CAN_SEE_ALL')")
	Dataset findById(Long id);

	/**
	 * Find datasets by their ids.
	 *
	 * @param ids datasets ids.
	 * @return a list if datasets or an empty list.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetList(returnObject, 'CAN_SEE_ALL')")
	List<Dataset> findByIdIn(List<Long> id);

	/**
	 * Save a dataset.
	 *
	 * @param dataset dataset to create.
	 * @return created dataset.
	 */
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnNewDataset(#dataset, 'CAN_IMPORT'))")
	Dataset create(Dataset dataset) throws SolrServerException, IOException;

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
	Page<Dataset> findPage(final Pageable pageable);


	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetList(returnObject, 'CAN_SEE_ALL')")
	List<Dataset> findByStudyId(Long studyId);

	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	int countByStudyId(Long studyId);

	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudy(#studyId, 'CAN_SEE_ALL'))")
	List<VolumeByFormatDTO> getVolumeByFormat(Long studyId);

	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnStudies(#studyIds, 'CAN_SEE_ALL'))")
	Map<Long, List<VolumeByFormatDTO>> getVolumeByFormatByStudyId(List<Long> studyIds);

	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetList(returnObject, 'CAN_SEE_ALL')")
	List<Dataset> findByExaminationId(Long examinationId);

	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnDatasetAcquisition(#acquisitionId, 'CAN_SEE_ALL'))")
	List<Dataset> findByAcquisition(Long acquisitionId);
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterDatasetList(returnObject, 'CAN_SEE_ALL')")
	List<Dataset> findByStudycard(Long studycardId);
  
	/**
	 * Get database statistics
	 * 
	 * @return statistics
	 */
	@PreAuthorize("hasRole('ADMIN')")
	List<Object[]> queryStatistics(String studyNameInRegExp, String studyNameOutRegExp, String subjectNameInRegExp, String subjectNameOutRegExp) throws Exception;
	
	
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnDataset(#dataset.getId(), 'CAN_ADMINISTRATE'))")
	void deleteDatasetFromPacs(Dataset dataset) throws ShanoirException;
}
