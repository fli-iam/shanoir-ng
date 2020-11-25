package org.shanoir.ng.exporter.service;

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

	/**
	 * Creates a full BIDS folder with all the data
	 * @param studyId the study ID for which we create the BIDS folder
	 * @param studyName the study name for which we create the BIDS folder
	 * @return the folder File named [study_id]_[study_name]
	 */
	public File exportAsBids(Long studyId, String studyName) throws IOException ;

	/**
	 * This method has been created to update (delete and recreate from scratch) a folder BIDS after a change
	 * @param studyId the study ID to update
	 * @param studyName the studyname
	 * @return the Bids Folder
	 * @throws IOException when the generation fails
	 */
	File updateBidsFolder(Long studyId, String studyName) throws IOException;

}
