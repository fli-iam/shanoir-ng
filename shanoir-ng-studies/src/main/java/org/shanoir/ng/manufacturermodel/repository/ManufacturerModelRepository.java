package org.shanoir.ng.manufacturermodel.repository;

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.springframework.data.repository.CrudRepository;

/**
 * Implementation of custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelRepository extends CrudRepository<ManufacturerModel, Long>, ManufacturerModelRepositoryCustom {

}
