package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;

/**
 * Acquisition equipment service.
 * 
 * @author msimon
 *
 */
public interface AcquisitionEquipmentService {

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

}
