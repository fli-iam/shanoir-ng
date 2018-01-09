package org.shanoir.ng.dataset.modality;

import javax.transaction.Transactional;

import org.shanoir.ng.dataset.DatasetBaseRepository;

/**
 * Repository for MR datasets.
 *
 * @author msimon
 */
@Transactional
public interface MrDatasetRepository extends DatasetBaseRepository<MrDataset> {

}
