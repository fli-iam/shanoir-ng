package org.shanoir.ng.dataset;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for datasets.
 *
 * @author msimon
 */
public interface DatasetRepository extends CrudRepository<Dataset, Long>, DatasetRepositoryCustom {

}
