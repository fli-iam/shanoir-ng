package org.shanoir.uploader.dicom;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.uploader.dicom.query.ConfigBean;
//import org.shanoir.dicom.query.DicomQueryHelper;
//import org.shanoir.services.dicom.server.ConfigBean;
//import org.shanoir.services.dicom.server.Echo;
import org.shanoir.uploader.dicom.query.DcmQR;
import org.shanoir.uploader.dicom.query.DicomQueryHelper;
import org.shanoir.uploader.dicom.query.Echo;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.retrieve.DcmRcvManager;
import org.shanoir.util.ShanoirUtil;

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
	
	private File workFolder;
	
	public DicomServerClient(final Properties dicomServerProperties, final File workFolder) {
		config.initWithPropertiesFile(dicomServerProperties);
		this.workFolder = workFolder;
		dcmRcvManager.start(config, workFolder.getAbsolutePath());
	}
	
	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#echoDicomServer()
	 */
	@Override
	public boolean echoDicomServer() {
		Echo echo = new Echo();
		boolean success = echo.echo(config);
		logger.info("Echoing of the DICOM server was successful? -> " + success);
		return success;
	}
	
	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#queryDicomServer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Media queryDicomServer(
			final String patientName,
			final String patientID,
			final String studyDescription,
			final String seriesDescription,
			final String patientBirthDate,
			final String studyDate
			) throws Exception {
		DcmQR dcmqr = new DcmQR();
		DicomQueryHelper dQH = new DicomQueryHelper(dcmqr, config, "");
		Media media = new Media();
		media = (Media) dQH.populateDicomTree(
				patientName,
				studyDescription,
				seriesDescription,
				patientID,
				null, null, media,
				patientBirthDate,
				studyDate);
		return media;
	}

	/* (non-Javadoc)
	 * @see org.shanoir.uploader.dicom.IDicomServerClient#retrieveDicomFiles(java.util.Collection)
	 */
	@Override
	public synchronized List<String> retrieveDicomFiles(final Collection<Serie> selectedSeries, final File uploadFolder) {
		final DcmQR dcmqr = new DcmQR();
		final DicomQueryHelper dQH = new DicomQueryHelper(dcmqr, config, "");
		dcmRcvManager.setDestination(uploadFolder.getAbsolutePath());
		final List<String> retrievedDicomFiles = new ArrayList<String>();
		final List<String> oldFileNames = new ArrayList<String>();
		// Iterate over all series and send command for sending DICOM files.
		for (Serie serie : selectedSeries) {
			List<String> fileNamesForSerie = new ArrayList<String>();
			final String seriesInstanceUID = serie.getDescriptionMap().get("id");
			final String studyInstanceUID = serie.getStudyInstanceUID();
			try {
				// move files from server directly into uploadFolder
				boolean noError = getFilesFromServer(dcmqr, dQH, studyInstanceUID, seriesInstanceUID);
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
					serie.setFileNames(fileNamesForSerie);
					retrievedDicomFiles.addAll(fileNamesForSerie);
					oldFileNames.addAll(fileNamesForSerie);
					logger.info(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
							+ " DICOM files for serie " + seriesInstanceUID
							+ " was successful.\n\n");
				} else {
					logger.error(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
							+ " DICOM files for serie " + seriesInstanceUID
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
	private boolean getFilesFromServer(DcmQR dcmqr, DicomQueryHelper dQH, final String studyInstanceUID, final String seriesInstanceUID) throws Exception {
		final List<DicomObject> list;
		try{
			String[] argsArray = dQH.buildCommand("-S", true, null, studyInstanceUID, seriesInstanceUID);
			logger.info("\n\n C_MOVE, serie command: launching dcmqr with args: " + ShanoirUtil.arrayToString(argsArray)+"\n\n");
			list = dcmqr.query(argsArray);
			logger.debug("\n\n Dicom Query list:\n "+list.toString()+"\n");
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		if (list != null && !list.isEmpty()) {
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
