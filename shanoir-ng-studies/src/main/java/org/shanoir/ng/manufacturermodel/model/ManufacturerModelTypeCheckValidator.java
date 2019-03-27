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
		switch (manufacturerModel.getDatasetModalityType()) {
		case MR_DATASET:
			// Magnetic field mandatory for MR models
			if (manufacturerModel.getMagneticField() == null) {
				context.disableDefaultConstraintViolation();
		        context
		            .buildConstraintViolationWithTemplate("MR manufacturer model has no magnetic field!")
		            .addConstraintViolation();
				return false;
			}
		default:
			// No magnetic field for other models
			manufacturerModel.setMagneticField(null);
			return true;
		}
	}

}
