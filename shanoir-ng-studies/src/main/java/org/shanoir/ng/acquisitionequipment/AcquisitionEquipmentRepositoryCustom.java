package org.shanoir.ng.acquisitionequipment;

import java.util.List;

import org.shanoir.ng.shared.model.ItemRepositoryCustom;

/**
 * Repository for acquisition equipments.
 * 
 * @author msimon
 *
 */
public interface AcquisitionEquipmentRepositoryCustom extends ItemRepositoryCustom<AcquisitionEquipment> {

	/**
	 * Find acquisition equipments by a couple of field value.
	 * 
	 * @param fieldName1
	 *            searched field name1.
	 * @param fieldName2
	 * 		      searched field name2.
	 * @param value1
	 *            value1.
	 * @param value2
	 * 			  value2.
	 * @author yyao
	 */
	List<AcquisitionEquipment> findByCoupleOfFieldValue(String fieldName1, Object value1, String fieldName2, Object value2);

}
