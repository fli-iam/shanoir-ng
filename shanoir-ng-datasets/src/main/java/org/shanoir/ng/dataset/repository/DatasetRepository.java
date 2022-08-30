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

package org.shanoir.ng.dataset.repository;

import java.util.List;

import org.shanoir.ng.dataset.model.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DatasetRepository extends PagingAndSortingRepository<Dataset, Long>, DatasetRepositoryCustom {

	Page<Dataset> findByDatasetAcquisitionExaminationStudyIdIn(Iterable<Long> studyIds, Pageable pageable);

	Iterable<Dataset> findByDatasetAcquisitionExaminationStudyId(Long studyId);
	
	Iterable<Dataset> findByDatasetAcquisitionId(Long acquisitionId);
	
	Iterable<Dataset> findBydatasetAcquisitionStudyCardId(Long studycardId);

	Iterable<Dataset> findByDatasetAcquisitionStudyCardIdAndDatasetAcquisitionExaminationStudyIdIn(Long studycardId, List<Long> studyIds);

	void deleteByIdIn(List<Long> ids);


}