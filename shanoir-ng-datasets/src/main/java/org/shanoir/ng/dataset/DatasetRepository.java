package org.shanoir.ng.dataset;

import javax.transaction.Transactional;

/**
 * Repository for datasets.
 *
 * @author msimon
 */
@Transactional
public interface DatasetRepository extends DatasetBaseRepository<Dataset> {

}
