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

package org.shanoir.ng.preclinical.pathologies;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Refs service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class PathologyServiceImpl implements PathologyService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PathologyServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private PathologyRepository pathologiesRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		pathologiesRepository.delete(id);
	}

	@Override
	public List<Pathology> findAll() {
		return Utils.toList(pathologiesRepository.findAll());
	}

	@Override
	public List<Pathology> findBy(final String fieldName, final Object value) {
		return pathologiesRepository.findBy(fieldName, value);
	}

	@Override
	public Pathology findById(final Long id) {
		return pathologiesRepository.findOne(id);
	}

	@Override
	public Optional<Pathology> findByName(final String name) {
		return pathologiesRepository.findByName(name);
	}

	@Override
	public Pathology save(final Pathology pathology) throws ShanoirException {
		Pathology savedPathology = null;
		try {
			savedPathology = pathologiesRepository.save(pathology);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating  pathology:  ", dive);
			throw new ShanoirException("Error while creating  pathology:  ", dive);
		}
		return savedPathology;
	}

	@Override
	public Pathology update(final Pathology pathology) throws ShanoirException {
		final Pathology pathologyDb = pathologiesRepository.findOne(pathology.getId());
		updatePathologyValues(pathologyDb, pathology);
		try {
			pathologiesRepository.save(pathologyDb);
		} catch (Exception e) {
			LOG.error("Error while updating  pathology:  ", e);
			throw new ShanoirException("Error while updating  pathology:  ", e);
		}
		return pathologyDb;
	}

	private Pathology updatePathologyValues(final Pathology pathologyDb, final Pathology pathology) {
		pathologyDb.setName(pathology.getName());
		return pathologyDb;
	}

	/*
	 * Update Shanoir Old.
	 *
	 * @param template template.
	 *
	 * @return false if it fails, true if it succeed.
	 */
	@Override
	public boolean updateFromShanoirOld(final Pathology pathology) {
		return true;
		/*
		 * try { LOG.info("Send update to Shanoir Old");
		 * System.out.println("Send update to Shanoir Old :" + new
		 * ObjectMapper().writeValueAsString(pathology));
		 * rabbitTemplate.convertAndSend(RabbitMqConfiguration.pathologyQueueOut().
		 * getName(), new ObjectMapper().writeValueAsString(pathology)); return true; }
		 * catch (AmqpException e) { LOG.error("Cannot send Pathology " +
		 * pathology.getId() + " save/update to Shanoir Old on queue : " +
		 * RabbitMqConfiguration.queueOut().getName(), e); } catch
		 * (JsonProcessingException e) { LOG.error("Cannot send Pathology " +
		 * pathology.getId() +
		 * " save/update because of an error while serializing Pathology.", e); } return
		 * false;
		 */
	}

}
