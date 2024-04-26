package org.shanoir.uploader.dicom;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.uploader.dicom.query.ConfigBean;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
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
 * @author mkain
 *
 */
public class DicomServerClient implements IDicomServerClient {
	
	private static final Logger logger = LoggerFactory.getLogger(DicomServerClient.class);
	
	private ConfigBean config = new ConfigBean();
		
	private DcmRcvManager dcmRcvManager = new DcmRcvManager();
	
	private QueryPACSService queryPACSService = new QueryPACSService();
	
	private File workFolder;
	
	public DicomServerClient(final Properties dicomServerProperties, final File workFolder) throws MalformedURLException {
		logger.info("New DicomServerClient created with properties: " + dicomServerProperties.toString());
		config.initWithPropertiesFile(dicomServerProperties);
		this.workFolder = workFolder;
		// Initialize connection configuration parameters here: to be used for all queries
		DicomNode calling = new DicomNode(config.getLocalDicomServerAETCalling(), config.getLocalDicomServerHost(), config.getLocalDicomServerPort());
		DicomNode called = new DicomNode(config.getDicomServerAETCalled(), config.getDicomServerHost(), config.getDicomServerPort());
		// attention: we use calling here (== ShUp) to inform the DICOM server to send to ShUp,
		// who becomes the "called" afterwards from the point of view of the DICOM server (switch)
		queryPACSService.setDicomNodes(calling, called, config.getLocalDicomServerAETCalling());
		dcmRcvManager.configureAndStartSCPServer(config, workFolder.getAbsolutePath());
	}
	
	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#echoDicomServer()
	 */
	@Override
	public boolean echoDicomServer() {
		int port = Integer.valueOf(config.getDicomServerPort());
		boolean result = queryPACSService.queryECHO(config.getDicomServerAETCalled(), config.getDicomServerHost(), port, config.getLocalDicomServerAETCalling());
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

	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#queryDicomServer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Patient> queryDicomServer(
			final String modality,
			final String patientName,
			final String patientID,
			final String studyDescription,
			final String patientBirthDate,
			final String studyDate
			) throws Exception {
		DicomQuery query = new DicomQuery();
		query.setModality(modality);
		query.setPatientName(patientName);
		query.setPatientID(patientID);
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
		final List<String> retrievedDicomFiles = new ArrayList<String>();
		if (!selectedSeries.isEmpty()) {
			downloadFromDicomServer(selectedSeries);
			readDicomFilesFromDisk(selectedSeries, uploadFolder, retrievedDicomFiles);
		}		
		return retrievedDicomFiles;
	}

	private void readDicomFilesFromDisk(final Collection<SerieTreeNode> selectedSeries, final File uploadFolder,
			final List<String> retrievedDicomFiles) {
		final List<String> oldFileNames = new ArrayList<String>();
		// Iterate over all series and align the downloaded files
		for (SerieTreeNode serieTreeNode : selectedSeries) {
			List<String> fileNamesForSerie = new ArrayList<String>();
			final String seriesInstanceUID = serieTreeNode.getId();
			try {
				final FilenameFilter oldFileNamesAndDICOMFilter = createFileNameFilter(oldFileNames);
				File serieFolder = new File (uploadFolder.getAbsolutePath() + File.separator + seriesInstanceUID);
				if (serieFolder.exists()) {
					File[] newFileNames = serieFolder.listFiles(oldFileNamesAndDICOMFilter);
					logger.debug("newFileNames: " + newFileNames.length);
					for (int i = 0; i < newFileNames.length; i++) {
						fileNamesForSerie.add(newFileNames[i].getName());
					}
					serieTreeNode.setFileNames(fileNamesForSerie);
					serieTreeNode.getSerie().setImagesNumber(fileNamesForSerie.size());
					retrievedDicomFiles.addAll(fileNamesForSerie);
					oldFileNames.addAll(fileNamesForSerie);
					logger.info(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
							+ " DICOM files for serie " + seriesInstanceUID + ": " + serieTreeNode.getDisplayString()
							+ " was successful.\n\n");
				} else {
					logger.error(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
					+ " DICOM files for serie " + seriesInstanceUID + ": " + serieTreeNode.getDisplayString()
					+ " has failed.\n\n");
				}
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Create file name filter for old files and only use .dcm files (ignore /tmp folder).
	 * 
	 * @param oldFileNames
	 * @return
	 */
	private FilenameFilter createFileNameFilter(final List<String> oldFileNames) {
		final FilenameFilter oldFileNamesAndDICOMFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// ignore files from other series before
				for (Iterator<String> iterator = oldFileNames.iterator(); iterator.hasNext();) {
					String oldFileName = (String) iterator.next();
					if (name.equals(oldFileName)) {
						return false;
					}
				}
				// only take .dcm files into consideration, ignore others
				if (name.endsWith(DcmRcvManager.DICOM_FILE_SUFFIX)) {
					return true;
				} else {
					return false;
				}
			}
		};
		return oldFileNamesAndDICOMFilter;
	}

	private void downloadFromDicomServer(final Collection<SerieTreeNode> selectedSeries) {
		final String studyInstanceUID = selectedSeries.iterator().next().getParent().getId();
		final List<String> seriesInstanceUIDs = new ArrayList<String>();
		selectedSeries.stream().forEach(s -> seriesInstanceUIDs.add(s.getId()));
		try {
			queryPACSService.queryCMOVEs(studyInstanceUID, seriesInstanceUIDs);
		} catch (Exception e) {
			logger.error(":\n\n Download of "
			+ " DICOM files for DICOM study/exam " + studyInstanceUID + ": " + " has failed.\n\n" + e.getMessage(), e);
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
