package org.shanoir.ng.shared.validation;

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
@Constraint(validatedBy = ManufactureModelTypeCheckValidator.class)
public @interface ManufactureModelTypeCheck {

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
