package org.shanoir.ng.preclinical.contrast_agent;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.references.ReferenceEnum;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
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

	private static final Logger LOG = LoggerFactory.getLogger(ContrastAgentApiController.class);

	@Autowired
	private ContrastAgentService contrastAgentService;
	@Autowired
	private RefsService referencesService;

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
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		contrastagent.setId(null);
		// Set the contrast agent protocol id
		contrastagent.setProtocolId(pid);

		/* Save contrastagent in db. */
		try {
			final ContrastAgent createdAgent = contrastAgentService.save(contrastagent);
			return new ResponseEntity<ContrastAgent>(createdAgent, HttpStatus.OK);
		} catch (ShanoirPreclinicalException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

	}

	public ResponseEntity<Void> deleteContrastAgent(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "Contrast Agent id to delete", required = true) @PathVariable("cid") Long cid) {
		if (contrastAgentService.findById(cid) == null) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}
		try {
			contrastAgentService.deleteById(cid);
		} catch (ShanoirPreclinicalException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<ContrastAgent> getContrastAgentByProtocolId(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid) {
		final ContrastAgent agent = contrastAgentService.findByProtocolId(pid);
		if (agent == null) {
			return new ResponseEntity<ContrastAgent>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<ContrastAgent>(agent, HttpStatus.OK);
	}

	public ResponseEntity<ContrastAgent> getContrastAgentById(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "ID of contrast agent that needs to be fetched", required = true) @PathVariable("cid") Long cid) {
		final ContrastAgent agent = contrastAgentService.findById(cid);
		if (agent == null) {
			return new ResponseEntity<ContrastAgent>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<ContrastAgent>(agent, HttpStatus.OK);
	}

	public ResponseEntity<ContrastAgent> getContrastAgentByName(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid,
			@ApiParam(value = "Name of contrast agent that needs to be fetched", required = true) @PathVariable("name") String name) {
		// Does name exists as reference
		Reference agent_name = referencesService.findByCategoryTypeAndValue(ReferenceEnum.REF_CATEGORY_CONTRASTAGENT,
				ReferenceEnum.REF_TYPE_CONTRASTAGENT_NAME, name);
		if (agent_name == null)
			return new ResponseEntity<ContrastAgent>(HttpStatus.NOT_FOUND);
		final ContrastAgent agent = contrastAgentService.findByName(agent_name);
		if (agent == null) {
			return new ResponseEntity<ContrastAgent>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<ContrastAgent>(agent, HttpStatus.OK);
	}

	public ResponseEntity<List<ContrastAgent>> getContrastAgents(
			@ApiParam(value = "protocol id", required = true) @PathVariable("pid") Long pid) {
		final List<ContrastAgent> agents = contrastAgentService.findAll();
		if (agents.isEmpty()) {
			return new ResponseEntity<List<ContrastAgent>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<ContrastAgent>>(agents, HttpStatus.OK);
	}

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
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			contrastAgentService.update(agent);
		} catch (ShanoirPreclinicalException e) {
			LOG.error("Error while trying to update contrast agent " + cid + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final ContrastAgent agent) {
		final ContrastAgent previousStateContrastAgent = contrastAgentService.findById(agent.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<ContrastAgent>()
				.validate(previousStateContrastAgent, agent);
		return accessErrors;
	}

	private FieldErrorMap getCreationRightsErrors(final ContrastAgent agent) {
		return new EditableOnlyByValidator<ContrastAgent>().validate(agent);
	}

	private FieldErrorMap getUniqueConstraintErrors(final ContrastAgent agent) {
		final UniqueValidator<ContrastAgent> uniqueValidator = new UniqueValidator<ContrastAgent>(contrastAgentService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(agent);
		return uniqueErrors;
	}

}
