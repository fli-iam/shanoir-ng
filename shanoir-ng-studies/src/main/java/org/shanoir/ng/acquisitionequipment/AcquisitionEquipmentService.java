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

package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Acquisition equipment service.
 * 
 * @author msimon
 *
 */
public interface AcquisitionEquipmentService extends UniqueCheckableService<AcquisitionEquipment>{

	/**
	 * Delete an acquisition equipment.
	 * 
	 * @param id
	 *            acquisition equipment id.
	 * @throws ShanoirStudiesException
	 */
	void deleteById(Long id) throws ShanoirStudiesException;

	/**
	 * Get all the acquisition equipments.
	 * 
	 * @return a list of acquisition equipments.
	 */
	List<AcquisitionEquipment> findAll();

	/**
	 * Find acquisition equipment by its id.
	 *
	 * @param id
	 *            acquisition equipment id.
	 * @return an acquisition equipment or null.
	 */
	AcquisitionEquipment findById(Long id);

	/**
	 * Save an acquisition equipment.
	 *
	 * @param acquisitionEquipment
	 *            acquisition equipment to create.
	 * @return created acquisition equipment.
	 * @throws ShanoirStudiesException
	 */
	AcquisitionEquipment save(AcquisitionEquipment acquisitionEquipment) throws ShanoirStudiesException;

	/**
	 * Update an acquisition equipment.
	 *
	 * @param acquisitionEquipment
	 *            acquisition equipment to update.
	 * @return updated acquisition equipment.
	 * @throws ShanoirStudiesException
	 */
	AcquisitionEquipment update(AcquisitionEquipment acquisitionEquipment) throws ShanoirStudiesException;

	/**
	 * @param fieldName1
	 * @param value1
	 * @param fieldName2
	 * @param value2
	 * @return
	 * @author yyao
	 */
	List<AcquisitionEquipment> findByCoupleOfFieldValue(String fieldName1, Object value1, String fieldName2,
			Object value2);

}
