package org.shanoir.ng.manufacturermodel.service;

import java.util.List;

import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerServiceImpl extends BasicEntityServiceImpl<Manufacturer> implements ManufacturerService {

	@Override
	public List<Manufacturer> findBy(String fieldName, Object value) {
		return this.findBy(fieldName, value, Manufacturer.class);
	}

	@Override
	protected Manufacturer updateValues(Manufacturer from, Manufacturer to) {
		return to;
	}


}
