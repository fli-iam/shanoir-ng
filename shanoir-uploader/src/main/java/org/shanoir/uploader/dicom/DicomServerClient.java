package org.shanoir.uploader.dicom;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JProgressBar;

import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.dicom.query.ConfigBean;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.uploader.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.param.DicomNode;

/**
 * This class is the communication interface to the DICOM server.
 * As we have only one instance of the DicomServerClient in ShUp,
 * as we have only one port available to connect with and only one
 * AET to configure in the PACS, the method retrieveDicomFiles is
 * synchronized in case of multiple threads in ShUp (ImportFinishRunnable)
 * call the methods and another download of dicom files is still ongoing.
 * 
 * @author mkain
 *
 */
public class DicomServerClient implements IDicomServerClient {

	private static final Logger logger = LoggerFactory.getLogger(DicomServerClient.class);

	private ConfigBean config = new ConfigBean();

	private DcmRcvManager dcmRcvManager = new DcmRcvManager();

	private QueryPACSService queryPACSService = new QueryPACSService();

	private File workFolder;

	public DicomServerClient(final Properties dicomServerProperties, final File workFolder)
			throws MalformedURLException {
		logger.info("New DicomServerClient created with properties: " + dicomServerProperties.toString());
		config.initWithPropertiesFile(dicomServerProperties);
		this.workFolder = workFolder;
		// Initialize connection configuration parameters here: to be used for all
		// queries
		DicomNode calling = new DicomNode(config.getLocalDicomServerAETCalling(), config.getLocalDicomServerHost(),
				config.getLocalDicomServerPort());
		DicomNode called = new DicomNode(config.getDicomServerAETCalled(), config.getDicomServerHost(),
				config.getDicomServerPort());
		// attention: we use calling here (== ShUp) to inform the DICOM server to send to ShUp,
		// who becomes the "called" afterwards from the point of view of the DICOM server (switch)
		queryPACSService.setDicomNodes(calling, called, config.getLocalDicomServerAETCalling());
		dcmRcvManager.configureAndStartSCPServer(config, workFolder.getAbsolutePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#echoDicomServer()
	 */
	@Override
	public boolean echoDicomServer() {
		int port = Integer.valueOf(config.getDicomServerPort());
		boolean result = queryPACSService.queryECHO(config.getDicomServerAETCalled(), config.getDicomServerHost(), port,
				config.getLocalDicomServerAETCalling());
		if (result) {
			logger.info("Echoing of the DICOM server was successful? -> " + result);
		} else {
			logger.info("Echoing of the DICOM server was successful? -> " + result);
			return false;
		}
		return true;
	}

	@Override
	public boolean echoDicomServer(String calledAET, String hostName, int port, String callingAET) {
		boolean result = queryPACSService.queryECHO(calledAET, hostName, port, callingAET);
		if (result) {
			logger.info("Echoing of the DICOM server was successful? -> " + result);
		} else {
			logger.info("Echoing of the DICOM server was successful? -> " + result);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.shanoir.uploader.dicom.IDicomServerClient#queryDicomServer(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Patient> queryDicomServer(
		final boolean studyRootQuery, 
		final String modality,
		final String patientName,
		final String patientID,
		final String studyDescription,
		final String patientBirthDate,
		final String studyDate) throws Exception {
		DicomQuery query = new DicomQuery();
		query.setStudyRootQuery(studyRootQuery);
		query.setPatientName(patientName);
		query.setPatientID(patientID);
		query.setPatientBirthDate(patientBirthDate);
		query.setStudyDescription(studyDescription);
		query.setStudyDate(studyDate);
		query.setModality(modality);
		return queryPACSService.queryCFIND(query).getPatients();
	}

	public List<Patient> queryDicomServer(DicomQuery query) throws Exception {
		return queryPACSService.queryCFIND(query).getPatients();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.shanoir.uploader.dicom.IDicomServerClient#retrieveDicomFiles(java.util.
	 * Collection)
	 */
	@Override
	public List<String> retrieveDicomFiles(final JProgressBar progressBar, StringBuilder downloadOrCopyReport, String studyInstanceUID, List<Serie> selectedSeries, final File uploadFolder) {
		final List<String> retrievedDicomFiles = new ArrayList<String>();
		if (selectedSeries != null && !selectedSeries.isEmpty()) {
			try {
				FileUtil.cleanTempFolders(workFolder, studyInstanceUID);
				downloadFromDicomServer(studyInstanceUID, selectedSeries, progressBar, downloadOrCopyReport);
				FileUtil.readAndCopyDicomFilesToUploadFolder(workFolder, studyInstanceUID, selectedSeries, uploadFolder, retrievedDicomFiles, downloadOrCopyReport);
				FileUtil.deleteFolderDownloadFromDicomServer(workFolder, studyInstanceUID, selectedSeries);
			} catch (Exception e) {
				logger.error(":\n\n Download of "
						+ " DICOM files for DICOM study/exam " + studyInstanceUID + ": " + " has failed.\n\n"
						+ e.getMessage(), e);
				return null;
			}				
		}
		return retrievedDicomFiles;
	}

	private void downloadFromDicomServer(String studyInstanceUID, List<Serie> selectedSeries, final JProgressBar progressBar, StringBuilder downloadOrCopyReport) throws Exception {
		// c-move: download images from DICOM server for all series
		// we have to call here for all series as the connection set up
		// and release is very time consuming and error prone, so we do
		// it only once in QueryPACSService.
		queryPACSService.queryCMOVEs(studyInstanceUID, selectedSeries, progressBar);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#getWorkFolder()
	 */
	@Override
	public File getWorkFolder() {
		return workFolder;
	}

}
