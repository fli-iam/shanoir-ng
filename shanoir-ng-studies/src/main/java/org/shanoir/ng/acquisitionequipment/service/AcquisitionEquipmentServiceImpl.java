package org.shanoir.ng.acquisitionequipment.service;

import java.util.List;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Acquisition equipment service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class AcquisitionEquipmentServiceImpl extends BasicEntityServiceImpl<AcquisitionEquipment> implements AcquisitionEquipmentService {


	@Override
	protected AcquisitionEquipment updateValues(AcquisitionEquipment from, AcquisitionEquipment to) {
		to.setCenter(from.getCenter());
		to.setManufacturerModel(from.getManufacturerModel());
		to.setSerialNumber(from.getSerialNumber());
		return to;
	}

	@Override
	public List<AcquisitionEquipment> findBy(String fieldName, Object value) {
		return this.findBy(fieldName, value, AcquisitionEquipment.class);
	}

}
