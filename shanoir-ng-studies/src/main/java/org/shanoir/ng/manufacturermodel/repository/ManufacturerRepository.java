package org.shanoir.ng.manufacturermodel.repository;

import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.springframework.data.repository.CrudRepository;

/**
 * Implementation of custom repository for centers.
 * 
 * @author msimon
 *
 */
public interface ManufacturerRepository extends CrudRepository<Manufacturer, Long> {

}
