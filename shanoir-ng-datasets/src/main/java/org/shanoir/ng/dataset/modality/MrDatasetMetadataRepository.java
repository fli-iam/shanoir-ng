package org.shanoir.ng.dataset.modality;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for MR dataset metadata.
 *
 * @author msimon
 */
public interface MrDatasetMetadataRepository extends CrudRepository<MrDatasetMetadata, Long> {

}
