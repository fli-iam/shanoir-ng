package org.shanoir.ng.manufacturermodel;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for manufacturers.
 * 
 * @author msimon
 *
 */
public interface ManufacturerRepository extends CrudRepository<Manufacturer, Long>, ManufacturerRepositoryCustom {

}
