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

package org.shanoir.ng.preclinical.extra_data.bloodgas_data;

import java.util.List;

import org.shanoir.ng.preclinical.extra_data.ExtraDataService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BloodGasData service implementation.
 * 
 * @author sloury
 *
 */
@Service
@Transactional
public class BloodGasDataServiceImpl implements ExtraDataService<BloodGasData> {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(BloodGasDataServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private BloodGasDataRepository bloodGasRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		bloodGasRepository.delete(id);
	}

	@Override
	public List<BloodGasData> findAllByExaminationId(Long id) {
		return Utils.toList(bloodGasRepository.findAllByExaminationId(id));
	}

	@Override
	public List<BloodGasData> findBy(final String fieldName, final Object value) {
		return bloodGasRepository.findBy(fieldName, value);
	}

	@Override
	public BloodGasData findById(final Long id) {
		return bloodGasRepository.findOne(id);
	}

	@Override
	public BloodGasData save(final BloodGasData extradata) throws ShanoirException {
		BloodGasData savedPhysioData = null;
		try {
			savedPhysioData = bloodGasRepository.save(extradata);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating blood gas data:  ", dive);
			throw new ShanoirException("Error while creating blood gas data:  ", dive);
		}
		return savedPhysioData;
	}

	@Override
	public BloodGasData update(final BloodGasData extradata) throws ShanoirException {
		final BloodGasData bloodgasDataDB = bloodGasRepository.findOne(extradata.getId());
		updateBloodGasDataValues(bloodgasDataDB, extradata);
		try {
			bloodGasRepository.save(bloodgasDataDB);
		} catch (Exception e) {
			LOG.error("Error while updating blood gas data:  ", e);
			throw new ShanoirException("Error while updating blood gas data:  ", e);
		}
		return bloodgasDataDB;
	}

	private BloodGasData updateBloodGasDataValues(final BloodGasData bloodGasDataDb, final BloodGasData bloodGasData) {
		bloodGasDataDb.setExaminationId(bloodGasData.getExaminationId());
		bloodGasDataDb.setExtradatatype(bloodGasData.getExtradatatype());
		bloodGasDataDb.setFilename(bloodGasData.getFilename());
		bloodGasDataDb.setFilepath(bloodGasData.getFilepath());
		return bloodGasDataDb;
	}

}
