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

package org.shanoir.ng.shared.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Field error map.
 * 
 * @author msimon
 *
 */
public class FieldErrorMap extends HashMap<String, List<FieldError>> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public FieldErrorMap() {
		super();
	}

	/**
	 * Constructor
	 */
	public FieldErrorMap(FieldErrorMap... maps) {
		this.merge(maps);
	}

	/**
	 * Constructor
	 *
	 * @param {@link
	 * 			BindingResult}
	 * @return {@link FieldErrorMap}
	 */
	public FieldErrorMap(BindingResult result) {
		if (result.hasErrors()) {
			for (ObjectError objectError : result.getAllErrors()) {
				org.springframework.validation.FieldError fieldError = (org.springframework.validation.FieldError) objectError;
				if (!this.containsKey(fieldError.getField())) {
					this.put(fieldError.getField(), new ArrayList<org.shanoir.ng.shared.error.FieldError>());
				}
				this.get(fieldError.getField()).add(new org.shanoir.ng.shared.error.FieldError(fieldError.getCode(),
						fieldError.getDefaultMessage(), fieldError.getRejectedValue()));
			}
		}
	}

	/**
	 * Merge errors properly
	 *
	 * @param maps
	 */
	public void merge(FieldErrorMap... maps) {
		for (FieldErrorMap map : maps) {
			for (String fieldName : map.keySet()) {
				List<FieldError> error = map.get(fieldName);
				if (!this.containsKey(fieldName)) {
					this.put(fieldName, error);
				} else {
					this.get(fieldName).addAll(error);
				}
			}
		}
	}
}
