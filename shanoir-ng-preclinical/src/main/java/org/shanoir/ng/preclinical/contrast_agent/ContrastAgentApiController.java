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

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.references.ReferenceEnum;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiParam;

@Controller
public class ContrastAgentApiController implements ContrastAgentApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(ContrastAgentApiController.class);

	@Autowired
	private ContrastAgentService contrastAgentService;
	@Autowired
	private RefsService referencesService;

	@Autowired
	private ContrastAgentUniqueValidator uniqueValidator;
	
	@Autowired
	private ContrastAgentEditableByManager editableOnlyValidator;


	@Override
	public ResponseEntity<ContrastAgent> createContrastAgent(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "Contrast Agent to create", required = true) @RequestBody ContrastAgent contrastagent,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(contrastagent);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(contrastagent);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		contrastagent.setId(null);
		// Set the contrast agent protocol id
		contrastagent.setProtocolId(pid);

		/* Save contrastagent in db. */
		try {
			final ContrastAgent createdAgent = contrastAgentService.save(contrastagent);
			return new ResponseEntity<>(createdAgent, HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

	}

	@Override
	public ResponseEntity<Void> deleteContrastAgent(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "Contrast Agent id to delete", required = true) @PathVariable("cid") Long cid) {
		if (contrastAgentService.findById(cid) == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		try {
			contrastAgentService.deleteById(cid);
		} catch (ShanoirException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ContrastAgent> getContrastAgentByProtocolId(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid) {
		final ContrastAgent agent = contrastAgentService.findByProtocolId(pid);
		if (agent == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(agent, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ContrastAgent> getContrastAgentById(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "ID of contrast agent that needs to be fetched", required = true) @PathVariable("cid") Long cid) {
		final ContrastAgent agent = contrastAgentService.findById(cid);
		if (agent == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(agent, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ContrastAgent> getContrastAgentByName(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "Name of contrast agent that needs to be fetched", required = true) @PathVariable("name") String name) {
		// Does name exists as reference
		Reference agentName = referencesService.findByCategoryTypeAndValue(ReferenceEnum.REF_CATEGORY_CONTRASTAGENT,
				ReferenceEnum.REF_TYPE_CONTRASTAGENT_NAME, name);
		if (agentName == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		final ContrastAgent agent = contrastAgentService.findByName(agentName);
		if (agent == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(agent, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<ContrastAgent>> getContrastAgents(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid) {
		final List<ContrastAgent> agents = contrastAgentService.findAll();
		if (agents.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(agents, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateContrastAgent(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "ID of contrast agent that needs to be updated", required = true) @PathVariable("cid") Long cid,
			@ApiParam(value = "Contrast Agent object that needs to be updated", required = true) @RequestBody ContrastAgent agent,
			final BindingResult result) throws RestServiceException {

		agent.setId(cid);
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(agent);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(agent);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		try {
			contrastAgentService.update(agent);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update contrast agent " + cid + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final ContrastAgent agent) {
	    return editableOnlyValidator.validate(agent);
	}

	private FieldErrorMap getCreationRightsErrors(final ContrastAgent agent) {
	    return editableOnlyValidator.validate(agent);
	}

	private FieldErrorMap getUniqueConstraintErrors(final ContrastAgent agent) {
		return uniqueValidator.validate(agent);
	}

}
