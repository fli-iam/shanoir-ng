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

package org.shanoir.ng.shared.util;

/**
 * Constants definition class.
 *
 * @author aferial
 */
public final class ShanoirConstants {

	/**
	 * The Enum DICOM_RETURNED_TYPES.
	 */
	public enum DICOM_RETURNED_TYPES {

		/** The BYT e_ ARRAY. */
		BYTE_ARRAY,
		/** The DATE. */
		DATE,
		/** The DAT e_ ARRAY. */
		DATE_ARRAY,
		/** The DAT e_ RANGE. */
		DATE_RANGE,
		/** The DOUBLE. */
		DOUBLE,
		/** The DOUBL e_ ARRAY. */
		DOUBLE_ARRAY,
		/** The FLOAT. */
		FLOAT,
		/** The FLOA t_ ARRAY. */
		FLOAT_ARRAY,
		/** The INT. */
		INT,
		/** The IN t_ ARRAY. */
		INT_ARRAY,
		/** The SHOR t_ ARRAY. */
		SHORT_ARRAY,
		/** The STRING. */
		STRING,
		/** The STRIN g_ ARRAY. */
		STRING_ARRAY
	}

	/**
	 * Hiding the constructor.
	 */
	private ShanoirConstants() {

	}
}
