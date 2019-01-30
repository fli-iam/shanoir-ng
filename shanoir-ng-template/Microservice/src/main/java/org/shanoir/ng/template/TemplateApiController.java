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

package org.shanoir.ng.template;

import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirTemplateException;
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
public class TemplateApiController implements TemplateApi {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateApiController.class);

	@Autowired
	private TemplateService templateService;

	@Override
	public ResponseEntity<Void> deleteTemplate(
			@ApiParam(value = "id of the template", required = true) @PathVariable("templateId") final Long templateId) {
		if (templateService.findById(templateId) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			templateService.deleteById(templateId);
		} catch (ShanoirTemplateException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Template> findTemplateById(
			@ApiParam(value = "id of the template", required = true) @PathVariable("templateId") final Long templateId) {
		final Template template = templateService.findById(templateId);
		if (template == null) {
			return new ResponseEntity<Template>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Template>(template, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Template>> findTemplates() {
		final List<Template> templates = templateService.findAll();
		if (templates.isEmpty()) {
			return new ResponseEntity<List<Template>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Template>>(templates, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Template> saveNewTemplate(
			@ApiParam(value = "the template to create", required = true) @RequestBody @Valid final Template template,
			final BindingResult result) throws RestServiceException {

		/* Validation */
		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getCreationRightsErrors(template);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(template);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		template.setId(null);

		/* Save template in db. */
		try {
			final Template createdTemplate = templateService.save(template);
			return new ResponseEntity<Template>(createdTemplate, HttpStatus.OK);
		} catch (ShanoirTemplateException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
	}

	@Override
	public ResponseEntity<Void> updateTemplate(
			@ApiParam(value = "id of the template", required = true) @PathVariable("templateId") final Long templateId,
			@ApiParam(value = "the template to update", required = true) @RequestBody @Valid final Template template,
			final BindingResult result) throws RestServiceException {

		// IMPORTANT : avoid any confusion that could lead to security breach
		template.setId(templateId);

		// A basic template can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(template);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		// Check unique constrainte
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(template);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update template in db. */
		try {
			templateService.update(template);
		} catch (ShanoirTemplateException e) {
			LOG.error("Error while trying to update template " + templateId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors.
	 *
	 * @param template template.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final Template template) {
		final Template previousStateTemplate = templateService.findById(template.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Template>().validate(previousStateTemplate, template);
		return accessErrors;
	}

	/*
	 * Get access rights errors.
	 *
	 * @param template template.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getCreationRightsErrors(final Template template) {
		return new EditableOnlyByValidator<Template>().validate(template);
	}

	/*
	 * Get unique constraint errors
	 *
	 * @param template
	 * 
	 * @return an error map
	 */
	private FieldErrorMap getUniqueConstraintErrors(final Template template) {
		final UniqueValidator<Template> uniqueValidator = new UniqueValidator<Template>(templateService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(template);
		return uniqueErrors;
	}

}
