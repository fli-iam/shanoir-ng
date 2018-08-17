package org.shanoir.ng.dataset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository for datasets.
 *
 * @author msimon
 */
public interface DatasetRepository extends DatasetBaseRepository<Dataset> {

	Page<Dataset> findAll(Pageable pageable);

}
