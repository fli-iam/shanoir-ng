package org.shanoir.ng.shared.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExtensionWithMotivationValidator.class)
public @interface ExtensionWithMotivation {

	/**
	 * Message.
	 */
	String message() default "Motivation is mandatory for extension request";

	/**
	 * Groups.
	 */
	Class<?>[] groups() default {};

	/**
	 * Payload.
	 */
	Class<? extends Payload>[] payload() default {};

}
