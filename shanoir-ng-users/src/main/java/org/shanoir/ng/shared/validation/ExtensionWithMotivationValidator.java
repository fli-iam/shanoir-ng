package org.shanoir.ng.shared.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.shanoir.ng.user.model.User;

/**
 * Validate if motivation exists with extension request.
 * 
 * @author msimon
 *
 */
public class ExtensionWithMotivationValidator implements ConstraintValidator<ExtensionWithMotivation, User> {

	@Override
	public void initialize(final ExtensionWithMotivation constraintAnnotation) {
	}

	@Override
	public boolean isValid(final User user, final ConstraintValidatorContext context) {
//		if (user.isExtensionRequest() && StringUtils.isEmpty(user.getExtensionMotivation())) {
//			// Motivation is mandatory if extension request
//			return false;
//		}
		return true;
	}

}
