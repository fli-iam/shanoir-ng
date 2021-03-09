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
	 * Outdates a study (or deletes it simply) when an update is performed on the study:
	 * Update, delete subject & study
	 * Add or delete dataset / examination
	 * @param  the shnoir event
	 */
	public void deleteBids(String event);

}
