package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudyException;

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
	 * @throws ShanoirStudyException
	 */
	void deleteById(Long id) throws ShanoirStudyException;

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
	 * @throws ShanoirStudyException
	 */
	AcquisitionEquipment save(AcquisitionEquipment acquisitionEquipment) throws ShanoirStudyException;

	/**
	 * Update an acquisition equipment.
	 *
	 * @param acquisitionEquipment
	 *            acquisition equipment to update.
	 * @return updated acquisition equipment.
	 * @throws ShanoirStudyException
	 */
	AcquisitionEquipment update(AcquisitionEquipment acquisitionEquipment) throws ShanoirStudyException;

}
