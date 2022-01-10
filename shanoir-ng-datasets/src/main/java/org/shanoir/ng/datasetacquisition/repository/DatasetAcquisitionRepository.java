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

import java.util.List;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for dataset acquisition.
 * 
 * @author msimon
 *
 */
public interface DatasetAcquisitionRepository extends PagingAndSortingRepository<DatasetAcquisition, Long>  {

	List<DatasetAcquisition> findByStudyCardId(Long studyCardId);
	
	List<DatasetAcquisition> findByExaminationId(Long studyCardId);
	
	Page<DatasetAcquisition> findByExaminationStudyIdIn(Iterable<Long> studyIds, Pageable pageable);

}
