package org.shanoir.ng.manufacturermodel.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation checking if manufacturer model is correct depending on its type.
 * 
 * @author msimon
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ManufacturerModelTypeCheckValidator.class)
public @interface ManufacturerModelTypeCheck {

	/**
	 * Message.
	 */
	String message() default "Manufacturer model not correct depending on its type.";

	/**
	 * Groups.
	 */
	Class<?>[] groups() default {};

	/**
	 * Payload.
	 */
	Class<? extends Payload>[] payload() default {};

}
