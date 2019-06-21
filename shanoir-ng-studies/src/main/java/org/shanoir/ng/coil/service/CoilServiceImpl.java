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

package org.shanoir.ng.coil.service;

import java.util.Optional;

import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.repository.CoilRepository;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * center service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class CoilServiceImpl extends BasicEntityServiceImpl<Coil> implements CoilService {

	@Autowired
	private CoilRepository coilRepository;

	@Override
	protected Coil updateValues(final Coil from, final Coil to) {
		to.setCoilType(from.getCoilType());
		to.setName(from.getName());
		to.setNumberOfChannels(from.getNumberOfChannels());
		to.setSerialNumber(from.getSerialNumber());
		to.setCenter(from.getCenter());
		to.setManufacturerModel(from.getManufacturerModel());
		return to;
	}

	@Override
	public Optional<Coil> findByName(String name) {
		return coilRepository.findByName(name);
	}
}
