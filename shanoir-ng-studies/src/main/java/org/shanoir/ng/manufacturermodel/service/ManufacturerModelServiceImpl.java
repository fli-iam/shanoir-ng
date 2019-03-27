package org.shanoir.ng.manufacturermodel.service;

import java.util.List;

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.repository.ManufacturerModelRepository;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerModelServiceImpl extends BasicEntityServiceImpl<ManufacturerModel> implements ManufacturerModelService {
	
	@Autowired
	private ManufacturerModelRepository manufacturerModelRepository;


	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return manufacturerModelRepository.findIdsAndNames();
	}

	@Override
	public List<IdNameDTO> findIdsAndNamesForCenter(Long centerId) {
		return manufacturerModelRepository.findIdsAndNames();
	}

	@Override
	public List<ManufacturerModel> findBy(String fieldName, Object value) {
		return this.findBy(fieldName, value, ManufacturerModel.class);
	}

	@Override
	protected ManufacturerModel updateValues(ManufacturerModel from, ManufacturerModel to) {
		to.setDatasetModalityType(from.getDatasetModalityType());
		to.setMagneticField(from.getMagneticField());
		to.setManufacturer(from.getManufacturer());
		to.setName(from.getName());
		return to;
	}

}
