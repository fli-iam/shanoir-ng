package org.shanoir.ng.datasetacquisition.mr;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for MR protocol metadata.
 *
 * @author msimon
 */
public interface MrProtocolSCMetadataRepository extends CrudRepository<MrProtocolSCMetadata, Long> {

}
