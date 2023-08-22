package org.shanoir.uploader.dicom;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dcm4che3.net.Status;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.uploader.dicom.query.ConfigBean;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.weasis.dicom.param.DicomNode;
import org.weasis.dicom.param.DicomState;

/**
 * This class is the communication interface to the DICOM server.
 * As we have only one instance of the DicomServerClient in ShUp,
 * as we have only one port available to connect with and only one
 * AET to configure in the PACS, the method retrieveDicomFiles is
 * synchronized in case of multiple threads in ShUp (ImportFinishRunnable)
 * call the methods and another download of dicom files is still ongoing.
 * @author mkain
 *
 */
public class DicomServerClient implements IDicomServerClient {
	
	private static Logger logger = Logger.getLogger(DicomServerClient.class);
	
	private ConfigBean config = new ConfigBean();
		
	private DcmRcvManager dcmRcvManager = new DcmRcvManager();
	
	private QueryPACSService queryPACSService;
	
	private File workFolder;
	
	public DicomServerClient(final Properties dicomServerProperties, final File workFolder) {
		config.initWithPropertiesFile(dicomServerProperties);
		this.workFolder = workFolder;
		// Initialize connection configuration parameters here: to be used for all queries
		DicomNode calling = new DicomNode(config.getLocalDicomServerAETCalling(), config.getLocalDicomServerHost(), config.getLocalDicomServerPort());
		DicomNode called = new DicomNode(config.getDicomServerAETCalled(), config.getDicomServerHost(), config.getDicomServerPort());
		queryPACSService = new QueryPACSService();
		queryPACSService.setDicomNodes(calling, called);
		dcmRcvManager.configure(config);
	}
	
	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#echoDicomServer()
	 */
	@Override
	public boolean echoDicomServer() {
		// @todo add echo here
		logger.info("Echoing of the DICOM server was successful? -> ");
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#queryDicomServer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Patient> queryDicomServer(
			final String patientName,
			final String patientID,
			final String studyDescription,
			final String seriesDescription,
			final String patientBirthDate,
			final String studyDate
			) throws Exception {
		DicomQuery query = new DicomQuery();
		query.setPatientID(patientID);
		query.setPatientName(patientName);
		query.setPatientBirthDate(patientBirthDate);
		query.setStudyDescription(studyDescription);
		query.setStudyDate(studyDate);
		return queryPACSService.queryCFIND(query).getPatients();
	}

	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#retrieveDicomFiles(java.util.Collection)
	 */
	@Override
	public synchronized List<String> retrieveDicomFiles(final Collection<SerieTreeNode> selectedSeries, final File uploadFolder) {
		dcmRcvManager.setDestination(uploadFolder.getAbsolutePath());
		final List<String> retrievedDicomFiles = new ArrayList<String>();
		final List<String> oldFileNames = new ArrayList<String>();
		// Iterate over all series and send command for sending DICOM files.
		for (SerieTreeNode serieTreeNode : selectedSeries) {
			List<String> fileNamesForSerie = new ArrayList<String>();
			final String seriesInstanceUID = serieTreeNode.getId();
			try {
				// move files from server directly into uploadFolder
				boolean noError = getFilesFromServer(seriesInstanceUID, serieTreeNode.getDescription());
				if(noError) {
					// create file name filter for old files
					final FilenameFilter oldFileNamesFilter = new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							for (Iterator iterator = oldFileNames.iterator(); iterator.hasNext();) {
								String oldFileName = (String) iterator.next();
								if (name.equals(oldFileName)) {
									return false;
								}
							}
							return true;
						}
					};
					File[] newFileNames = uploadFolder.listFiles(oldFileNamesFilter);
					logger.debug("newFileNames: " + newFileNames.length);
					for (int i = 0; i < newFileNames.length; i++) {
						fileNamesForSerie.add(newFileNames[i].getName());
					}
					serieTreeNode.setFileNames(fileNamesForSerie);
					retrievedDicomFiles.addAll(fileNamesForSerie);
					oldFileNames.addAll(fileNamesForSerie);
					logger.info(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
							+ " DICOM files for serie " + seriesInstanceUID + ": " + serieTreeNode.getDisplayString()
							+ " was successful.\n\n");
				} else {
					logger.error(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
							+ " DICOM files for serie " + seriesInstanceUID + ": " + serieTreeNode.getDisplayString()
							+ " has failed.\n\n");
					return null;
				}
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}
		return retrievedDicomFiles;
	}
	
	/**
	 * DICOM query for receiving all image files (C-MOVE) for one serie.
	 * @param dcmqr
	 * @param dQH
	 * @param seriesInstanceUID
	 * @return
	 */
	private boolean getFilesFromServer(final String seriesInstanceUID, final String seriesDescription) throws Exception {
		final DicomState state;
		try {
			logger.info("\n\n C_MOVE, serie (" + seriesDescription + ") command: launching dcmqr with args:\n\n");
			state = queryPACSService.queryCMOVE(seriesInstanceUID);
			logger.debug("\n\n Dicom Query list:\n " + state.toString() + "\n");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		if (state != null && state.getStatus() == Status.Success) {
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#getWorkFolder()
	 */
	@Override
	public File getWorkFolder() {
		return workFolder;
	}
	
}
