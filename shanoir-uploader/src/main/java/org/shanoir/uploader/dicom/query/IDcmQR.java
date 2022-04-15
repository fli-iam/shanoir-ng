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

package org.shanoir.uploader.dicom.query;

import java.util.List;

import org.dcm4che2.data.DicomObject;

public interface IDcmQR {
	
	/**
	 * Call the main method of DcmQr.
	 *
	 * @param args
	 *            the args
	 *
	 * @throws Exception
	 *             the exception
	 */
	void main(final String[] args) throws Exception;

	/**
	 * Perform a C-FIND the query method of DcmQr.
	 *
	 * @param args
	 *            the args
	 *
	 * @return the list< dicom object>
	 *
	 * @throws Exception
	 *             the exception
	 */
	List<DicomObject> query(final String[] args) throws Exception;

	/**
	 * Removes.
	 */
	void remove();
	
}
