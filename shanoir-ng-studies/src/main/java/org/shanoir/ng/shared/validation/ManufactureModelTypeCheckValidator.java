package org.shanoir.ng.shared.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.shanoir.ng.manufacturermodel.ManufacturerModel;

/**
 * Validate if manufacturer model is correct depending on its type.
 * 
 * @author msimon
 *
 */
public class ManufactureModelTypeCheckValidator
		implements ConstraintValidator<ManufactureModelTypeCheck, ManufacturerModel> {

	@Override
	public void initialize(final ManufactureModelTypeCheck constraintAnnotation) {
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
			return true;
		}
	}

}
