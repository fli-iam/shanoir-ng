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
		return to;
	}

	@Override
	public Optional<Coil> findByName(String name) {
		return coilRepository.findByName(name);
	}
}
