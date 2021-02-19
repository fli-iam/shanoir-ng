package org.shanoir.uploader.dicom;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.shanoir.dicom.importer.Serie;
import org.shanoir.uploader.dicom.query.Media;

public interface IDicomServerClient {

	/**
	 * This method echos a DICOM server.
	 * @return
	 */
	public boolean echoDicomServer();

	/**
	 * This method queries a DICOM server and returns a tree structured Media object.
	 * @param patientName
	 * @param patientID
	 * @param studyDescription
	 * @param seriesDescription
	 * @return Media object representing a tree model structure or null
	 */
	public Media queryDicomServer(String patientName,
			String patientID, String studyDescription, String seriesDescription, String birthDate, String studyDate)
			throws Exception;

	/**
	 * This method initializes the download of DICOM files from the DICOM server.
	 * After this query has been sent to the DICOM server, the DICOM server will
	 * begin to send all files to a local server implemented in DcmRcvHelper.
	 * @param selectedSeries
	 */
	public List<String> retrieveDicomFiles(Collection<Serie> selectedSeries, final File tempFolderForUpload);
	
	/**
	 * This method returns the work folder.
	 * @return
	 */
	public File getWorkFolder();

}