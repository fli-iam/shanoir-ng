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

package org.shanoir.ng.center.service;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * center service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class CenterServiceImpl extends BasicEntityServiceImpl<Center> implements CenterService {

	@Autowired
	private CenterRepository centerRepository;


	@Override
	public void deleteByIdCheckDependencies(final Long id) throws EntityNotFoundException, UndeletableDependenciesException {
		final Center center = centerRepository.findOne(id);
		if (center == null) {
			throw new EntityNotFoundException(Center.class, id);
		}
		final List<FieldError> errors = new ArrayList<>();
		if (!center.getAcquisitionEquipments().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "acquisitionEquipments"));
		}
		if (!center.getStudyCenterList().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "studies"));
		}
		if (!errors.isEmpty()) {
			final FieldErrorMap errorMap = new FieldErrorMap();
			errorMap.put("delete", errors);
			throw new UndeletableDependenciesException(errorMap);
		}
		centerRepository.delete(id);
	}

	@Override
	public List<IdName> findIdsAndNames() {
		return centerRepository.findIdsAndNames();
	}
	
	@Override
	public List<IdName> findIdsAndNames(Long studyId) {
		return centerRepository.findIdsAndNames(studyId);
	}

	@Override
	protected Center updateValues(final Center from, final Center to) {
		to.setCity(from.getCity());
		to.setCountry(from.getCountry());
		to.setName(from.getName());
		to.setPhoneNumber(from.getPhoneNumber());
		to.setPostalCode(from.getPostalCode());
		to.setStreet(from.getStreet());
		to.setWebsite(from.getWebsite());
		return to;
	}

	@Override
	public Center findByName(String name) {
		return centerRepository.findByName(name);
	}
}
