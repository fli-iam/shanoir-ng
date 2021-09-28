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

package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
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
public class AnestheticIngredientApiController implements AnestheticIngredientApi {

	private static final String BAD_ARGUMENTS = "Bad arguments";

	private static final Logger LOG = LoggerFactory.getLogger(AnestheticIngredientApiController.class);

	@Autowired
	private AnestheticIngredientService ingredientsService;
	@Autowired
	private AnestheticService anestheticsService;
	@Autowired
	private AnestheticIngredientUniqueValidator uniqueValidator;
	
	@Autowired
	private AnestheticIngredientEditableByManager editableOnlyValidator;


	@Override
	public ResponseEntity<AnestheticIngredient> createAnestheticIngredient(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Anesthetic Ingredient to create", required = true) @RequestBody AnestheticIngredient ingredient,
			BindingResult result) throws RestServiceException {

		// First check if given anesthetic exists
		Anesthetic anesthetic = anestheticsService.findById(id);
		if (anesthetic == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Anesthetic not found", new ErrorDetails()));
		} else {
			final FieldErrorMap accessErrors = this.getCreationRightsErrors(ingredient);
			final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
			final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(ingredient);
			/* Merge errors. */
			final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
			if (!errors.isEmpty()) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS,
						new ErrorDetails(errors)));
			}

			// Guarantees it is a creation, not an update
			ingredient.setId(null);
			try {
				ingredient.setAnesthetic(anesthetic);
			} catch (Exception e) {
				LOG.error("Error while parsing anesthetic id for Long cast " + e.getMessage(), e);
			}
			/* Save ingredient in db. */
			try {
				final AnestheticIngredient createdIngredient = ingredientsService.save(ingredient);
				return new ResponseEntity<>(createdIngredient, HttpStatus.OK);
			} catch (ShanoirException e) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
			}
		}
	}

	@Override
	public ResponseEntity<Void> deleteAnestheticIngredient(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Anesthetic Ingredient id to delete", required = true) @PathVariable("aiid") Long aiid) {
		AnestheticIngredient toDelete = ingredientsService.findById(aiid);
		if (toDelete == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		try {
			ingredientsService.deleteById(toDelete.getId());
		} catch (ShanoirException e) {
			return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<AnestheticIngredient> getAnestheticIngredientById(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of ingredient that needs to be fetched", required = true) @PathVariable("aiid") Long aiid) {
		final AnestheticIngredient ingredient = ingredientsService.findById(aiid);
		if (ingredient == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(ingredient, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<AnestheticIngredient>> getAnestheticIngredients(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id)
			throws RestServiceException {
		// First check if given anesthetic exists
		Anesthetic anesthetic = anestheticsService.findById(id);
		if (anesthetic == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NO_CONTENT.value(), "Anesthetic not found", new ErrorDetails()));
		} else {
			final List<AnestheticIngredient> ingredients = ingredientsService.findByAnesthetic(anesthetic);
			return new ResponseEntity<>(ingredients, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Void> updateAnestheticIngredient(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of anesthetic ingredient that needs to be updated", required = true) @PathVariable("aiid") Long aiid,
			@ApiParam(value = "Anesthetic Ingredient object that needs to be updated", required = true) @RequestBody AnestheticIngredient ingredient,
			final BindingResult result) throws RestServiceException {

		ingredient.setId(aiid);
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(ingredient);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(ingredient);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, new ErrorDetails(errors)));
		}

		try {
			ingredientsService.update(ingredient);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update ingredient " + aiid + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), BAD_ARGUMENTS, null));
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final AnestheticIngredient ingredient) {
	    return editableOnlyValidator.validate(ingredient);
	}

	private FieldErrorMap getCreationRightsErrors(final AnestheticIngredient ingredient) {
	    return editableOnlyValidator.validate(ingredient);
	}

	private FieldErrorMap getUniqueConstraintErrors(final AnestheticIngredient ingredient) {
		return uniqueValidator.validate(ingredient);
	}

}
