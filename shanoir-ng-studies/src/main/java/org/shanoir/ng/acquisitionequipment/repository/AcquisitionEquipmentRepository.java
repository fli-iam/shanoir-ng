/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.acquisitionequipment.repository;

import java.util.List;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for coils.
 *
 * @author msimon
 */
public interface AcquisitionEquipmentRepository extends CrudRepository<AcquisitionEquipment, Long> {

	List<AcquisitionEquipment> findByCenterId(Long centerId);

	List<AcquisitionEquipment> findByCenterStudyCenterListStudyId(Long studyId);

	List<AcquisitionEquipment> findBySerialNumberContaining(String serialNumber);

	List<AcquisitionEquipment> findByManufacturerModelId(Long manufacturerModelId);

	@Modifying
	@Query("DELETE FROM AcquisitionEquipment ae WHERE ae.id = :id")
	public void deleteById(Long id);
}
