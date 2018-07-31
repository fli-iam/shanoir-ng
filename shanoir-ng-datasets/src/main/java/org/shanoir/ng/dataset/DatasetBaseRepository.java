package org.shanoir.ng.dataset;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository for datasets.
 *
 * @author msimon
 */
@NoRepositoryBean
public interface DatasetBaseRepository<T extends Dataset> extends PagingAndSortingRepository<T, Long> {

}
