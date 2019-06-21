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

package org.shanoir.ng.shared.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.shanoir.ng.user.User;

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
