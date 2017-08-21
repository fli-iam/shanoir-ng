package org.shanoir.ng.Import.anonymization;

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
	 * Anonymize a list of DICOM files
	 *
	 * @param dicomFiles
	 *            the list of Dicom files to anonymize
	 * @param profile
	 *            the anonymization profile
	 */

	void anonymize(ArrayList<File> dicomFiles, String profile);

}
