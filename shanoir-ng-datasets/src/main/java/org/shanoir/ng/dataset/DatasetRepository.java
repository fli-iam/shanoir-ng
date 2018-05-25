package org.shanoir.ng.dataset;

import org.springframework.data.domain.Pageable;

/**
 * Repository for datasets.
 *
 * @author msimon
 */
public interface DatasetRepository extends DatasetBaseRepository<Dataset> {

	Iterable<Dataset> findAll(Pageable pageable);

}
