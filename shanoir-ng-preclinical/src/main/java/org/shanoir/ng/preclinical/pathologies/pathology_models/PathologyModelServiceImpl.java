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

package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Pathology models service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class PathologyModelServiceImpl implements PathologyModelService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PathologyModelServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private PathologyModelRepository modelsRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		modelsRepository.deleteById(id);
	}

	@Override
	public List<PathologyModel> findAll() {
		return Utils.toList(modelsRepository.findAll());
	}

	@Override
	public List<PathologyModel> findBy(final String fieldName, final Object value) {
		return modelsRepository.findBy(fieldName, value);
	}

	@Override
	public PathologyModel findById(final Long id) {
		return modelsRepository.findById(id).orElse(null);
	}

	@Override
	public List<PathologyModel> findByPathology(Pathology pathology) {
		return Utils.toList(modelsRepository.findByPathology(pathology));
	}

	@Override
	public PathologyModel save(final PathologyModel model) throws ShanoirException {
		PathologyModel savedModel = null;
		try {
			savedModel = modelsRepository.save(model);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating pathology model:  ", dive);
			throw new ShanoirException("Error while creating pathology model:  ", dive);
		}
		return savedModel;
	}

	@Override
	public PathologyModel update(final PathologyModel model) throws ShanoirException {
		final PathologyModel modelDb = modelsRepository.findById(model.getId()).orElse(null);
		updateModelValues(modelDb, model);
		try {
			modelsRepository.save(modelDb);
		} catch (Exception e) {
			LOG.error("Error while updating pathology model:  ", e);
			throw new ShanoirException("Error while updating pathology model:  ", e);
		}
		return modelDb;
	}

	private PathologyModel updateModelValues(final PathologyModel modelDb, final PathologyModel model) {
		modelDb.setName(model.getName());
		modelDb.setComment(model.getComment());
		modelDb.setFilename(model.getFilename());
		modelDb.setFilepath(model.getFilepath());
		modelDb.setPathology(model.getPathology());
		return modelDb;
	}

}
