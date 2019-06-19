package org.shanoir.ng.manufacturermodel.model;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates if a manufacturer model is correct depending on its type.
 * 
 * @author msimon
 *
 */
public class ManufacturerModelTypeCheckValidator
		implements ConstraintValidator<ManufacturerModelTypeCheck, ManufacturerModel> {

	@Override
	public void initialize(final ManufacturerModelTypeCheck constraintAnnotation) {
	}

	@Override
	public boolean isValid(final ManufacturerModel manufacturerModel, final ConstraintValidatorContext context) {
		
		return true;
	}

}
