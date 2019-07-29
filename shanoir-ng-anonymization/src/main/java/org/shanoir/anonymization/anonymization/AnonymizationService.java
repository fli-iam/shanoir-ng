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

package org.shanoir.anonymization.anonymization;

import java.io.File;
import java.util.ArrayList;

/**
 * Anonymization service.
 * 
 * @author ifakhfakh
 * 
 */


public interface AnonymizationService {
	
	/**
	 * Anonymize a list of DICOM files according to the basic profile recommendations
	 *
	 * @param dicomFiles
	 *            the list of Dicom files to anonymize
	 * @param profile
	 *            the anonymization profile
	 * @throws Exception 
	 */

	void anonymize(ArrayList<File> dicomFiles, String profile) throws Exception;

	/**
	 * Anonymize a list of DICOM files taking into account Shanoir's constraints to use and store anonymized data 
	 * 
	 * @param dicomFiles
	 * 			the list of Dicom files to anonymize
	 * @param profile
	 * 			the anonymization profile
	 * @param patientFirstName
	 * 			the new patient firstName
	 * @param patientLastName
	 * 			the new patient lastName
	 * @param patientID
	 * 			the new patient id
	 * @throws Exception 
	 */
	void anonymizeForShanoir(ArrayList<File> dicomFiles, String profile, String patientFirstName,
			String patientLastName, String patientID) throws Exception;
	
	void anonymizeForShanoir(ArrayList<File> dicomFiles, String profile, String patientName, String patientID) throws Exception;

}
