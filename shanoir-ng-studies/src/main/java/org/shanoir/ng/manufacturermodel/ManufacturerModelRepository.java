package org.shanoir.ng.manufacturermodel;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for manufacturer models.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelRepository extends CrudRepository<ManufacturerModel, Long>, ManufacturerModelRepositoryCustom {

}
