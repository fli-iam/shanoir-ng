package org.shanoir.ng.datasetacquisition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Annotation checking if datasets of an acquisition have same modality type
 * than the acquisition.
 * 
 * @author msimon
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DatasetsModalityTypeCheckValidator.class)
public @interface DatasetsModalityTypeCheck {

	/**
	 * Message.
	 */
	String message() default "Some datasets have not same modality type than acquisition.";

	/**
	 * Groups.
	 */
	Class<?>[] groups() default {};

	/**
	 * Payload.
	 */
	Class<? extends Payload>[] payload() default {};

}
