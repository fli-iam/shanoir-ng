package org.shanoir.ng.dataset.repository;

import org.shanoir.ng.dataset.model.Dataset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DatasetRepository extends PagingAndSortingRepository<Dataset, Long> {

	Page<Dataset> findByStudyIdIn(Iterable<Long> studyIds, Pageable pageable);

} 