package org.shanoir.ng.dataset.modality;

import javax.transaction.Transactional;

import org.shanoir.ng.dataset.DatasetRepository;

/**
 * Repository for PET datasets.
 *
 * @author msimon
 */
@Transactional
public interface PetDatasetRepository extends DatasetRepository<PetDataset> {

}
