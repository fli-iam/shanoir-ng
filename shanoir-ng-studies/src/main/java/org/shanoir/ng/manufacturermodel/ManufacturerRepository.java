package org.shanoir.ng.manufacturermodel;

import org.shanoir.ng.shared.model.ItemRepositoryCustom;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for manufacturers.
 * 
 * @author msimon
 *
 */
public interface ManufacturerRepository extends CrudRepository<Manufacturer, Long>, ItemRepositoryCustom<Manufacturer> {

}
