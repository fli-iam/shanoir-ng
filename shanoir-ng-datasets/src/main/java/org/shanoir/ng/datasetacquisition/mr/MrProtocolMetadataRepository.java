package org.shanoir.ng.datasetacquisition.mr;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for MR protocol metadata.
 *
 * @author msimon
 */
public interface MrProtocolMetadataRepository extends CrudRepository<MrProtocolMetadata, Long> {

}
