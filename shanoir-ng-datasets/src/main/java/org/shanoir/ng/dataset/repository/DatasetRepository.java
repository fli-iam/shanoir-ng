package org.shanoir.ng.dataset.repository;

import org.shanoir.ng.dataset.model.Dataset;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DatasetRepository extends PagingAndSortingRepository<Dataset, Long> {

} 