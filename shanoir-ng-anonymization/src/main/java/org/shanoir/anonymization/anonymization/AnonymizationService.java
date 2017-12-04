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
	 */

	void anonymize(ArrayList<File> dicomFiles, String profile);

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
	 */
	void anonymizeForShanoir(ArrayList<File> dicomFiles, String profile, String patientFirstName,
			String patientLastName, String patientID);

}
