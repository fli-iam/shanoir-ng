package org.shanoir.ng.acquisitionequipment;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for acquisition equipments.
 * 
 * @author msimon
 *
 */
public interface AcquisitionEquipmentRepository extends CrudRepository<AcquisitionEquipment, Long>, AcquisitionEquipmentRepositoryCustom {

}
