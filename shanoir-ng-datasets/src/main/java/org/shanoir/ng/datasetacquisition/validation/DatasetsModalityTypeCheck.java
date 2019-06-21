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

package org.shanoir.ng.datasetacquisition.validation;

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
