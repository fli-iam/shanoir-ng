package org.shanoir.uploader.dicom;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.dicom.query.ConfigBean;
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
		// attention: we use calling here (== ShUp) to inform the DICOM server to send
		// to ShUp,
		// who becomes the "called" afterwards from the point of view of the DICOM
		// server (switch)
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
			final String modality,
			final String patientName,
			final String patientID,
			final String studyDescription,
			final String patientBirthDate,
			final String studyDate) throws Exception {
		DicomQuery query = new DicomQuery();
		query.setModality(modality);
		query.setPatientName(patientName);
		query.setPatientID(patientID);
		query.setPatientBirthDate(patientBirthDate);
		query.setStudyDescription(studyDescription);
		query.setStudyDate(studyDate);
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
	public List<String> retrieveDicomFiles(String studyInstanceUID, List<Serie> selectedSeries, final File uploadFolder) {
		final List<String> retrievedDicomFiles = new ArrayList<String>();
		if (selectedSeries != null && !selectedSeries.isEmpty()) {
			try {
				downloadFromDicomServer(studyInstanceUID, selectedSeries);
				readAndCopyDicomFilesToUploadFolder(studyInstanceUID, selectedSeries, uploadFolder, retrievedDicomFiles);
				deleteFolderDownloadFromDicomServer(studyInstanceUID, selectedSeries);
			} catch (Exception e) {
				logger.error(":\n\n Download of "
						+ " DICOM files for DICOM study/exam " + studyInstanceUID + ": " + " has failed.\n\n"
						+ e.getMessage(), e);
			}				
		}
		return retrievedDicomFiles;
	}

	private void deleteFolderDownloadFromDicomServer(String studyInstanceUID, List<Serie> selectedSeries) throws IOException {
		if (selectedSeries != null && !selectedSeries.isEmpty()) {
			File studyFolder = new File(workFolder + File.separator + studyInstanceUID);
			try (Stream<Path> walk = Files.walk(studyFolder.toPath())) {
				walk.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
			}
		}
	}

	private void readAndCopyDicomFilesToUploadFolder(String studyInstanceUID, List<Serie> selectedSeries, final File uploadFolder,
			final List<String> retrievedDicomFiles) throws IOException {
		for (Serie serie : selectedSeries) {
			List<String> fileNamesForSerie = new ArrayList<String>();
			final String seriesInstanceUID = serie.getSeriesInstanceUID();
			File serieFolder = new File(workFolder
				+ File.separator + studyInstanceUID
				+ File.separator + seriesInstanceUID);
			if (serieFolder.exists()) {
				File[] serieFiles = serieFolder.listFiles();
				for (int i = 0; i < serieFiles.length; i++) {
					String dicomFileName = serieFiles[i].getName();
					fileNamesForSerie.add(dicomFileName);
					File sourceFileFromPacs = serieFiles[i];
					File destSerieFolder = new File(uploadFolder.getAbsolutePath()
						+ File.separator + seriesInstanceUID);
					if (!destSerieFolder.exists())
						destSerieFolder.mkdirs();
					File destDicomFile = new File(destSerieFolder, dicomFileName);
					Files.move(sourceFileFromPacs.toPath(), destDicomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				retrievedDicomFiles.addAll(fileNamesForSerie);
				logger.info(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
						+ " DICOM files for serie " + seriesInstanceUID + ": " + serie.getSeriesDescription()
						+ " was successful.\n\n");
			} else {
				logger.error(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
						+ " DICOM files for serie " + seriesInstanceUID + ": " + serie.getSeriesDescription()
						+ " has failed.\n\n");
			}
		}
	}

	private void downloadFromDicomServer(String studyInstanceUID, List<Serie> selectedSeries) throws Exception {
		final List<String> seriesInstanceUIDs = new ArrayList<String>();
		selectedSeries.stream().forEach(s -> seriesInstanceUIDs.add(s.getSeriesInstanceUID()));
		queryPACSService.queryCMOVEs(studyInstanceUID, seriesInstanceUIDs);
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
