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

package org.shanoir.ng.shared.exception;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * SecurityException exception.
 *
 * @author jlouis
 *
 */
public class EntityFoundException extends ShanoirException {

	private static final long serialVersionUID = -497761359516114600L;

	public EntityFoundException(String message) {
		super(message);
	}

	public EntityFoundException(Class<? extends AbstractEntity> clazz, Long id) {
		super(getMessage(clazz, id));
	}

	private static String getMessage(Class<? extends AbstractEntity> clazz, Long id) {
		return "Cannot find "
				+ clazz.getSimpleName()
				+ " with id "
				+ id;
	}

}
