package org.shanoir.ng.bids.service;

import java.io.File;
import java.io.IOException;

/**
 * Interface defining the BIDS service methods.
 * -> Generate full BIDS folder (base + data)
 * -> Add a dataset (only data)
 * -> delete dataset
 * -> delete exam (loop call on delete dataset method)
 * @author JComeD
 *
 */
public interface BIDSService {

	File getBidsFolderpath(Long studyId, String studyName);

	/**
	 * Creates a full BIDS folder with all the data
	 * @param studyId the study ID for which we create the BIDS folder
	 * @param studyName the study name for which we create the BIDS folder
	 * @return the folder File named [study_id]_[study_name]
	 */
	public File exportAsBids(Long studyId, String studyName) throws IOException ;

	/**
	 * Deletes the folder of a given study to update / delete
	 * @param studyId the given study ID
	 * @param studyName the given study name
	 */
	void deleteBidsFolder(Long studyId, String studyName);

}
