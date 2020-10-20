package org.shanoir.ng.exporter.service;

import java.io.File;
import java.io.IOException;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.examination.model.Examination;

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
	 * When importing a dataset, add it to the current BIDS folder
	 * @param exam the examination/dataset to add.
	 * @param subjectName the subject name
	 * @param studyName the studyName
	 * @return the folder File named [study_id]_[study_name]
	 * @throws IOException when an error happens when adding the files
	 */
	File addDataset(Examination exam, String subjectName, String studyName) throws IOException;

	/**
	 * Deletes a dataset from a BIDS folder.
	 * @param dataset the dataset to delete
	 * @return true if deleted, false otherwise
	 */
	public void deleteDataset(Dataset dataset);

	/**
	 * Deletes all datasets linked to an examination in a BIDS folder
	 * @param exam the examination to delete
	 * @return true if deleted, false otherwise
	 */
	public void deleteExam(Long examId);

	/**
	 * This method has been created to update (delete and recreate from scratch) a folder BIDS after a change
	 * @param studyId the study ID to update
	 * @param studyName the studyname
	 * @return the Bids Folder
	 * @throws IOException when the generation fails
	 */
	File updateBidsFolder(Long studyId, String studyName) throws IOException;

}
