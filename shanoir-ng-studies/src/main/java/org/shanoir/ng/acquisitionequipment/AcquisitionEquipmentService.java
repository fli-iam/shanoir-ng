package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
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
	void deleteById(Long id) throws EntityNotFoundException;

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
