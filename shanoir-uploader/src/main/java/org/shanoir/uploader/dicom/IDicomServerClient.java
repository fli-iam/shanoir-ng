package org.shanoir.uploader.dicom;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.dicom.query.SerieTreeNode;

public interface IDicomServerClient {

	/**
	 * This method echos a DICOM server.
	 * @return 
	 */
	public boolean echoDicomServer();
	
	public boolean echoDicomServer(String calledAET, String hostName, int port, String callingAET);

	/**
	 * This method queries a DICOM server and returns a tree structured Media object.
	 * @param patientName
	 * @param patientID
	 * @param studyDescription
	 * @param seriesDescription
	 * @return Media object representing a tree model structure or null
	 */
	public List<Patient> queryDicomServer(String modality, String patientName, String patientID,
			String studyDescription, String birthDate, String studyDate) throws Exception;

	/**
	 * This method initializes the download of DICOM files from the DICOM server.
	 * After this query has been sent to the DICOM server, the DICOM server will
	 * begin to send all files to a local server implemented in DcmRcvHelper.
	 * @param selectedSeries
	 */
	public List<String> retrieveDicomFiles(String studyInstanceUID, List<Serie> selectedSeries, final File tempFolderForUpload);
	
	/**
	 * This method returns the work folder.
	 * @return
	 */
	public File getWorkFolder();

}