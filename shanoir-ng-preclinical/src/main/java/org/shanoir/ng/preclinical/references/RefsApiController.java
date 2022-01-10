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

package org.shanoir.ng.preclinical.references;

import java.util.List;

import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.KeycloakUtil;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T14:51:04.625Z")

@Controller
public class RefsApiController implements RefsApi {

	private static final Logger LOG = LoggerFactory.getLogger(RefsApiController.class);

	@Autowired
	private RefsService referenceService;

	@Autowired
	private ShanoirEventService eventService;

	@Autowired
	private ReferenceUniqueValidator uniqueValidator;

	@Autowired
	private RefsEditableByManager editableOnlyValidator;

	@Override
	public ResponseEntity<Reference> createReferenceValue(
			@ApiParam(value = "Ref value to add", required = true) @RequestBody Reference reference)
			throws RestServiceException {
		try {
			Reference checkRef = referenceService.findByCategoryTypeAndValue(reference.getCategory(),
					reference.getReftype(), reference.getValue());
			if (checkRef != null) {
				return new ResponseEntity<Reference>(checkRef, HttpStatus.OK);
			} else {
				Reference newRef = new Reference();
				newRef.setCategory(reference.getCategory().toLowerCase());
				newRef.setReftype(reference.getReftype().toLowerCase());
				newRef.setValue(reference.getValue());

				final Reference createdRef = referenceService.save(newRef);
				eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_PRECLINICAL_REFERENCE_EVENT, createdRef.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
				return new ResponseEntity<Reference>(createdRef, HttpStatus.OK);
			}
		} catch (ShanoirException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

	}

	@Override
	public ResponseEntity<Void> deleteReferenceValue(
			@ApiParam(value = "id of reference", required = true) @PathVariable("id") Long id) {

		Reference toDelete = referenceService.findById(id);
		if (toDelete == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			referenceService.deleteById(toDelete.getId());
			eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_PRECLINICAL_REFERENCE_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
		} catch (ShanoirException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Reference>> getReferencesByCategory(
			@ApiParam(value = "type of reference", required = true) @PathVariable("category") String category) {
		final List<Reference> refs = referenceService.findByCategory(category);
		if (refs.isEmpty()) {
			return new ResponseEntity<List<Reference>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Reference>>(refs, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Reference>> getReferencesByCategoryAndType(
			@ApiParam(value = "Category of the reference", required = true) @PathVariable("category") String category,
			@ApiParam(value = "Type of the reference", required = true) @PathVariable("type") String type) {
		final List<Reference> refs = referenceService.findByCategoryAndType(category, type);
		if (refs.isEmpty()) {
			return new ResponseEntity<List<Reference>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Reference>>(refs, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<String>> getReferenceCategories() {
		final List<String> categories = referenceService.findCategories();
		if (categories.isEmpty()) {
			return new ResponseEntity<List<String>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<String>>(categories, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<String>> getReferenceTypesByCategory(
			@ApiParam(value = "category of reference", required = true) @PathVariable("category") String category) {
		final List<String> types = referenceService.findTypesByCategory(category);
		if (types.isEmpty()) {
			return new ResponseEntity<List<String>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<String>>(types, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Reference>> getReferences() {
		final List<Reference> refs = referenceService.findAll();
		if (refs.isEmpty()) {
			return new ResponseEntity<List<Reference>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Reference>>(refs, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Reference> getReferenceById(
			@ApiParam(value = "id of reference", required = true) @PathVariable("id") Long id) {
		final Reference ref = referenceService.findById(id);
		if (ref == null) {
			return new ResponseEntity<Reference>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Reference>(ref, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Reference> getReferenceByCategoryTypeAndValue(
			@ApiParam(value = "category of reference", required = true) @PathVariable("category") String category,
			@ApiParam(value = "type of reference", required = true) @PathVariable("type") String type,
			@ApiParam(value = "value of reference type to be deleted", required = true) @PathVariable("value") String value) {
		final Reference ref = referenceService.findByCategoryTypeAndValue(category, type, value);
		if (ref == null) {
			return new ResponseEntity<Reference>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Reference>(ref, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateReferenceValue(
			@ApiParam(value = "id of ref to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "New value for the ref given as name", required = true) @RequestBody Reference reference,
			final BindingResult result) throws RestServiceException {
		Reference toUpdate = referenceService.findById(id);
		if (toUpdate == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} else {
			reference.setId(id);
			reference.setCategory(reference.getCategory().toLowerCase());
			reference.setReftype(reference.getReftype().toLowerCase());

			final FieldErrorMap accessErrors = this.getUpdateRightsErrors(reference);
			final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
			final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(reference);
			/* Merge errors. */
			final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
			if (!errors.isEmpty()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
						new ErrorDetails(errors)));
			}
			try {
				referenceService.update(reference);
				eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_PRECLINICAL_REFERENCE_EVENT, id.toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
			} catch (ShanoirException e) {
				LOG.error("Error while trying to update reference " + toUpdate.getId() + " : ", e);
				throw new RestServiceException(e,
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}

	private FieldErrorMap getUpdateRightsErrors(final Reference reference) {
	    return editableOnlyValidator.validate(reference);
	}

	private FieldErrorMap getUniqueConstraintErrors(final Reference reference) {
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(reference);
		return uniqueErrors;
	}

}
