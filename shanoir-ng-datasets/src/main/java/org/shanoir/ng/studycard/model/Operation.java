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

package org.shanoir.ng.studycard.model;

public enum Operation {

	STARTS_WITH(1),
	EQUALS(2),
	ENDS_WITH(3),
	CONTAINS(4),
	SMALLER_THAN(5),
	BIGGER_THAN(6);
	
	
	private int id;

	
	private Operation(final int id) {
		this.id = id;
	}

	/**
	 * Get an operation type by its id.
	 * 
	 * @param id type id.
	 * @return operation type.
	 */
	public static Operation getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (Operation type : Operation.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching operation type for id " + id);
	}

	public int getId() {
		return id;
	}

}
