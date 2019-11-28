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

package org.shanoir.ng.preclinical.extra_data.physiological_data;

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
 * PhysiologicalData service implementation.
 * 
 * @author sloury
 *
 */
@Service
@Transactional
public class PhysiologicalDataServiceImpl implements ExtraDataService<PhysiologicalData> {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PhysiologicalDataServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private PhysiologicalDataRepository physioDataRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		physioDataRepository.delete(id);
	}

	@Override
	public List<PhysiologicalData> findAllByExaminationId(Long id) {
		return Utils.toList(physioDataRepository.findAllByExaminationId(id));
	}

	@Override
	public List<PhysiologicalData> findBy(final String fieldName, final Object value) {
		return physioDataRepository.findBy(fieldName, value);
	}

	@Override
	public PhysiologicalData findById(final Long id) {
		return physioDataRepository.findOne(id);
	}

	@Override
	public PhysiologicalData save(final PhysiologicalData extradata) throws ShanoirException {
		PhysiologicalData savedPhysioData = null;
		try {
			savedPhysioData = physioDataRepository.save(extradata);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating examination extra data:  ", dive);
			throw new ShanoirException("Error while creating examination extra data:  ", dive);
		}
		return savedPhysioData;
	}

	@Override
	public PhysiologicalData update(final PhysiologicalData extradata) throws ShanoirException {
		final PhysiologicalData physiologicalDataDB = physioDataRepository.findOne(extradata.getId());
		updatePhysiologicalDataValues(physiologicalDataDB, extradata);
		try {
			physioDataRepository.save(physiologicalDataDB);
		} catch (Exception e) {
			LOG.error("Error while updating examination extra data:  ", e);
			throw new ShanoirException("Error while updating examination extra data:  ", e);
		}
		return physiologicalDataDB;
	}

	private PhysiologicalData updatePhysiologicalDataValues(final PhysiologicalData physioDataDb,
			final PhysiologicalData physioData) {
		physioDataDb.setExaminationId(physioData.getExaminationId());
		physioDataDb.setExtradatatype(physioData.getExtradatatype());
		physioDataDb.setFilename(physioData.getFilename());
		physioDataDb.setFilepath(physioData.getFilepath());
		physioDataDb.setHas_heart_rate(physioData.getHas_heart_rate());
		physioDataDb.setHas_respiratory_rate(physioData.getHas_respiratory_rate());
		physioDataDb.setHas_sao2(physioData.getHas_sao2());
		physioDataDb.setHas_temperature(physioData.getHas_temperature());
		return physioDataDb;
	}

}
