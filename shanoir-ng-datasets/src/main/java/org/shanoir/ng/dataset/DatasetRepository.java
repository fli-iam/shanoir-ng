package org.shanoir.ng.dataset;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repository for datasets.
 *
 * @author msimon
 */
@NoRepositoryBean
public interface DatasetRepository<T extends Dataset> extends CrudRepository<T, Long>, DatasetRepositoryCustom {

}
