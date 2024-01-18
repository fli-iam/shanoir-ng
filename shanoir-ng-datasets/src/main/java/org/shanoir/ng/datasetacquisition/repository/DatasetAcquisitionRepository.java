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

package org.shanoir.ng.datasetacquisition.repository;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Repository for dataset acquisition.
 * 
 * @author msimon
 *
 */
public interface DatasetAcquisitionRepository extends PagingAndSortingRepository<DatasetAcquisition, Long>, CrudRepository<DatasetAcquisition, Long>, DatasetAcquisitionRepositoryCustom  {

	List<DatasetAcquisition> findByStudyCardId(Long studyCardId);

	Page<DatasetAcquisition> findByStudyCardId(Long studyCardId, Pageable pageable);
	
	List<DatasetAcquisition> findByExaminationId(Long id);
	
	List<DatasetAcquisition> findDistinctByDatasetsIdIn(Long[] datasetIds);

    boolean existsByStudyCard_Id(Long studyCardId);

	List<DatasetAcquisition> findBySourceId(Long sourceId);
	DatasetAcquisition findBySourceIdAndExaminationStudy_Id(Long sourceId, Long studyId);
}


