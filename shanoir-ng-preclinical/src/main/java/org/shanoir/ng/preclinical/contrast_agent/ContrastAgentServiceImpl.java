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

package org.shanoir.ng.preclinical.contrast_agent;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Contrast Agent service implementation.
 * 
 * @author sloury
 *
 */
@Service
public class ContrastAgentServiceImpl implements ContrastAgentService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ContrastAgentServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ContrastAgentRepository contrastAgentsRepository;

	@Override
	public void deleteById(final Long id) throws ShanoirException {
		contrastAgentsRepository.delete(id);
	}

	@Override
	public List<ContrastAgent> findAll() {
		return Utils.toList(contrastAgentsRepository.findAll());
	}

	@Override
	public List<ContrastAgent> findBy(final String fieldName, final Object value) {
		return contrastAgentsRepository.findBy(fieldName, value);
	}

	@Override
	public ContrastAgent findByProtocolId(final Long protocolId) {
		Optional<ContrastAgent> ca = contrastAgentsRepository.findByProtocolId(protocolId);
		if (ca.isPresent())
			return ca.get();
		return null;
	}

	@Override
	public ContrastAgent findById(final Long id) {
		return contrastAgentsRepository.findOne(id);
	}

	@Override
	public ContrastAgent findByName(final Reference name) {
		Optional<ContrastAgent> ca = contrastAgentsRepository.findByName(name);
		if (ca.isPresent())
			return ca.get();
		return null;
	}

	@Override
	public ContrastAgent save(final ContrastAgent agent) throws ShanoirException {
		ContrastAgent savedAgent = null;
		try {
			savedAgent = contrastAgentsRepository.save(agent);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating contrast agent:  ", dive);
			throw new ShanoirException("Error while creating contrast agent:  ", dive);
		}
		return savedAgent;
	}

	@Override
	public ContrastAgent update(final ContrastAgent agent) throws ShanoirException {
		final ContrastAgent agentDb = contrastAgentsRepository.findOne(agent.getId());
		updateModelValues(agentDb, agent);
		try {
			contrastAgentsRepository.save(agentDb);
		} catch (Exception e) {
			LOG.error("Error while updating contrast agent:  ", e);
			throw new ShanoirException("Error while updating contrast agent:  ", e);
		}
		return agentDb;
	}

	private ContrastAgent updateModelValues(final ContrastAgent agentDb, final ContrastAgent agent) {
		agentDb.setName(agent.getName());
		agentDb.setManufacturedName(agent.getManufacturedName());
		agentDb.setConcentration(agent.getConcentration());
		agentDb.setConcentrationUnit(agent.getConcentrationUnit());
		agentDb.setDose(agent.getDose());
		agentDb.setDoseUnit(agent.getDoseUnit());
		agentDb.setInjectionInterval(agent.getInjectionInterval());
		agentDb.setInjectionSite(agent.getInjectionSite());
		agentDb.setInjectionType(agent.getInjectionType());
		return agentDb;
	}

}
