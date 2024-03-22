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

	STARTS_WITH(1, new DicomTagType[] {
		DicomTagType.String}),
	EQUALS(2, new DicomTagType[] {
		DicomTagType.String, 
		DicomTagType.Date, 
		DicomTagType.Double,
		DicomTagType.Float,
		DicomTagType.FloatArray,
		DicomTagType.IntArray,
		DicomTagType.Integer,
		DicomTagType.Long}),
	ENDS_WITH(3, new DicomTagType[] {
		DicomTagType.String}),
	CONTAINS(4, new DicomTagType[] {
		DicomTagType.String}),
	SMALLER_THAN(5, new DicomTagType[] {
		DicomTagType.Date, 
		DicomTagType.Double,
		DicomTagType.Float,
		DicomTagType.Integer,
		DicomTagType.Long}),
	BIGGER_THAN(6, new DicomTagType[] {
		DicomTagType.Date, 
		DicomTagType.Double,
		DicomTagType.Float,
		DicomTagType.Integer,
		DicomTagType.Long}),
	DOES_NOT_CONTAIN(7, new DicomTagType[] {
		DicomTagType.String}),
	DOES_NOT_START_WITH(8, new DicomTagType[] {
		DicomTagType.String}),
	NOT_EQUALS(9, new DicomTagType[] {
		DicomTagType.String, 
		DicomTagType.Date, 
		DicomTagType.Double,
		DicomTagType.Float,
		DicomTagType.FloatArray,
		DicomTagType.IntArray,
		DicomTagType.Integer,
		DicomTagType.Long}),
	DOES_NOT_END_WITH(10, new DicomTagType[] {
		DicomTagType.String}),
	PRESENT(11, new DicomTagType[] {		
		DicomTagType.String, 
		DicomTagType.Date, 
		DicomTagType.Double,
		DicomTagType.Float,
		DicomTagType.FloatArray,
		DicomTagType.IntArray,
		DicomTagType.Integer,
		DicomTagType.Long,
		DicomTagType.Binary}),
	ABSENT(12, new DicomTagType[] {
		DicomTagType.String, 
		DicomTagType.Date, 
		DicomTagType.Double,
		DicomTagType.Float,
		DicomTagType.FloatArray,
		DicomTagType.IntArray,
		DicomTagType.Integer,
		DicomTagType.Long,
		DicomTagType.Binary});
	
	private int id;
	private DicomTagType[] dicomTypeCompatibilities;
	
	private Operation(final int id, DicomTagType[] dicomTypeCompatibilities) {
		this.id = id;
		this.dicomTypeCompatibilities = dicomTypeCompatibilities;
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

	public DicomTagType[] getDicomTypeCompatibilities() {
		return dicomTypeCompatibilities;
	}

	public boolean compatibleWith(DicomTagType type) {
		for (DicomTagType myType : dicomTypeCompatibilities) {
			if (myType.equals(type)) {
				return true;
			}
		}
		return false;
	}
}
