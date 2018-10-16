package org.shanoir.ng.preclinical.anesthetics.ingredients;

import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
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
public class AnestheticIngredientApiController implements AnestheticIngredientApi {

	private static final Logger LOG = LoggerFactory.getLogger(AnestheticIngredientApiController.class);

	@Autowired
	private AnestheticIngredientService ingredientsService;
	@Autowired
	private AnestheticService anestheticsService;

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
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
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
				return new ResponseEntity<AnestheticIngredient>(createdIngredient, HttpStatus.OK);
			} catch (ShanoirException e) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
		}
	}

	public ResponseEntity<Void> deleteAnestheticIngredient(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Anesthetic Ingredient id to delete", required = true) @PathVariable("aiid") Long aiid) {
		AnestheticIngredient toDelete = ingredientsService.findById(aiid);
		if (toDelete == null) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		}
		try {
			ingredientsService.deleteById(toDelete.getId());
		} catch (ShanoirException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<AnestheticIngredient> getAnestheticIngredientById(
			@ApiParam(value = "anesthetic id", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "ID of ingredient that needs to be fetched", required = true) @PathVariable("aiid") Long aiid) {
		final AnestheticIngredient ingredient = ingredientsService.findById(aiid);
		if (ingredient == null) {
			return new ResponseEntity<AnestheticIngredient>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<AnestheticIngredient>(ingredient, HttpStatus.OK);
	}

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
			if (ingredients.isEmpty()) {
				return new ResponseEntity<List<AnestheticIngredient>>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<AnestheticIngredient>>(ingredients, HttpStatus.OK);
		}
	}

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
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			ingredientsService.update(ingredient);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update ingredient " + aiid + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	private FieldErrorMap getUpdateRightsErrors(final AnestheticIngredient ingredient) {
		final AnestheticIngredient previousStateIngredient = ingredientsService.findById(ingredient.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<AnestheticIngredient>()
				.validate(previousStateIngredient, ingredient);
		return accessErrors;
	}

	private FieldErrorMap getCreationRightsErrors(final AnestheticIngredient ingredient) {
		return new EditableOnlyByValidator<AnestheticIngredient>().validate(ingredient);
	}

	private FieldErrorMap getUniqueConstraintErrors(final AnestheticIngredient ingredient) {
		final UniqueValidator<AnestheticIngredient> uniqueValidator = new UniqueValidator<AnestheticIngredient>(
				ingredientsService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(ingredient);
		return uniqueErrors;
	}

}
