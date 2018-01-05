package org.shanoir.ng.dataset.modality;

import javax.transaction.Transactional;

import org.shanoir.ng.dataset.DatasetRepository;

/**
 * Repository for CT datasets.
 *
 * @author msimon
 */
@Transactional
public interface CtDatasetRepository extends DatasetRepository<CtDataset> {

}
