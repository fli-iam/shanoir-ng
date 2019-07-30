package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJobManager;
import org.shanoir.uploader.utils.Util;
import org.shanoir.util.ShanoirUtil;
import org.shanoir.util.file.FileUtil;

/**
 * This class downloads the files from the PACS or copies
 * them from the CD/DVD to an upload folder and creates the
 * upload-job.xml.
 * 
 * @author mkain
 *
 */
public class DownloadOrCopyRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(DownloadOrCopyRunnable.class);

	private static final String UNDERSCORE = "_";
	
	private boolean isFromPACS;
	
	private IDicomServerClient dicomServerClient;
	
	private String filePathDicomDir;

	private Set<org.shanoir.dicom.importer.Serie> selectedSeries;

	private DicomDataTransferObject dicomData;
	
	public DownloadOrCopyRunnable(boolean isFromPACS, final IDicomServerClient dicomServerClient, final String filePathDicomDir,
		final Set<org.shanoir.dicom.importer.Serie> selectedSeries, final DicomDataTransferObject dicomData) {
		this.isFromPACS = isFromPACS;
		this.dicomServerClient = dicomServerClient; // used with PACS import
		if(!isFromPACS && filePathDicomDir != null) {
			this.filePathDicomDir = new String(filePathDicomDir); // used with CD/DVD import
		}
		this.selectedSeries = selectedSeries;
		this.dicomData = dicomData;
	}

	public void run() {
		/**
		 * 1. Download from PACS or copy from CD/DVD
		 */
		File uploadFolder = createUploadFolder(dicomServerClient.getWorkFolder(), dicomData);
		List<String> allFileNames = downloadOrCopyFilesIntoUploadFolder(this.isFromPACS, selectedSeries, uploadFolder);
		
		/**
		 * 2. Fill MRI information into serie from first DICOM file of each serie
		 * This has already been done for CD/DVD import, but not yet here for PACS
		 */
		for (Iterator iterator = selectedSeries.iterator(); iterator.hasNext();) {
			Serie serie = (Serie) iterator.next();
			Util.processSerieMriInfo(uploadFolder, serie);
		}
		
		/**
		 * 3. Write the UploadJob and schedule upload
		 */
		UploadJob uploadJob = new UploadJob();
		initUploadJob(selectedSeries, dicomData, uploadJob);
		UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
		uploadJobManager.writeUploadJob(uploadJob);

		/**
		 * 4. Write the NominativeDataUploadJobManager for displaying the download state
		 */
		NominativeDataUploadJob dataJob = new NominativeDataUploadJob();
		initDataUploadJob(selectedSeries, dicomData, dataJob);
		NominativeDataUploadJobManager uploadDataJobManager = new NominativeDataUploadJobManager(
				uploadFolder.getAbsolutePath());
		uploadDataJobManager.writeUploadDataJob(dataJob);
		ShUpOnloadConfig.getCurrentNominativeDataController().addNewNominativeData(uploadFolder, dataJob);
		
		logger.info(uploadFolder.getName() + ": finished: " + toString());
	}
	
	private File createUploadFolder(final File workFolder, final DicomDataTransferObject dicomData) {
		final String timeStamp = ShanoirUtil.getCurrentTimeStampForFS();
		final String folderName = workFolder.getAbsolutePath() + File.separator + dicomData.getSubjectIdentifier()
				+ UNDERSCORE + timeStamp;
		File uploadFolder = new File(folderName);
		uploadFolder.mkdirs();
		logger.info("UploadFolder created: " + uploadFolder.getAbsolutePath());
		return uploadFolder;
	}

	/**
	 * In case of the PACS download the destination folder of the DCM server is set to the uploadFolder.
	 * This means, the DICOM files send from the PACS after the c-move, will directly be stored in the
	 * uploadFolder in the workFolder. Each file has the name of its sopInstanceUID and all files are in
	 * the same folder in the end (no sub-folders involved).
	 * In case of the CD/DVD the CD can contain multiple sub-folders with sub-folders, that are referenced
	 * from the DICOMDIR. Therefore ShUp copies the original DICOM files from their deep location, see array
	 * of Tag.ReferencedFileID to the uploadFolder in a flat way: the uploadFolder does not contain sub-folders.
	 * To avoid overwrites because of the same file name, the original path to the file is used as file name,
	 * separated by "_" underscores.
	 * 
	 * @param isFromPACS
	 * @param selectedSeries
	 * @param uploadFolder
	 * @return
	 */
	private List<String> downloadOrCopyFilesIntoUploadFolder(boolean isFromPACS, Set<Serie> selectedSeries, File uploadFolder) {
		List<String> allFileNames = null;
		if (isFromPACS) {
			allFileNames = dicomServerClient.retrieveDicomFiles(selectedSeries, uploadFolder);
			if(allFileNames != null) {
				logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files downloaded from PACS.");
			}
		} else {
			allFileNames = copyFilesToUploadFolderFromCD(selectedSeries, uploadFolder);
			if(allFileNames != null) {
				logger.info(uploadFolder.getName() + ": " + allFileNames.size() + " DICOM files copied from CD/DVD.");
			}
		}
		return allFileNames;
	}

	private List<String> copyFilesToUploadFolderFromCD(Set<org.shanoir.dicom.importer.Serie> selectedSeries, final File uploadFolder) {
		List<String> allFileNames = new ArrayList<String>();
		for (org.shanoir.dicom.importer.Serie serie : selectedSeries) {
			List<String> newFileNamesOfSerie = new ArrayList<String>();
			List<String> oldFileNamesOfSerie = serie.getFileNames();
			File sourceFile;
			File destFile;
			for (Iterator iterator = oldFileNamesOfSerie.iterator(); iterator.hasNext();) {
				String dicomFileName = (String) iterator.next();				
				sourceFile = new File(filePathDicomDir + File.separator + dicomFileName);
				dicomFileName = dicomFileName.replace(File.separator, UNDERSCORE);
				destFile = new File(uploadFolder.getAbsolutePath() + File.separator + dicomFileName);
				FileUtil.copyFile(sourceFile, destFile);
				newFileNamesOfSerie.add(dicomFileName);
			}
			serie.setFileNames(newFileNamesOfSerie);
			allFileNames.addAll(newFileNamesOfSerie);
		}
		return allFileNames;
	}
	
	/**
	 * Initializes UploadJob object to be written to file system.
	 * 
	 * @param selectedSeries
	 * @param dicomData
	 * @param uploadJob
	 */
	private void initUploadJob(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
			final DicomDataTransferObject dicomData, UploadJob uploadJob) {
		uploadJob.setUploadState(UploadState.READY);
		uploadJob.setUploadDate(ShanoirUtil.formatTimePattern(new Date()));
		/**
		 * Patient level
		 */
		// set hash of subject identifier in any case: pseudonymus mode or not
		uploadJob.setSubjectIdentifier(dicomData.getSubjectIdentifier());
		// set all 10 hash values for pseudonymus mode
		if (ShUpConfig.isModePseudonymus()) {
			uploadJob.setBirthNameHash1(dicomData.getBirthNameHash1());
			uploadJob.setBirthNameHash2(dicomData.getBirthNameHash2());
			uploadJob.setBirthNameHash3(dicomData.getBirthNameHash3());
			uploadJob.setLastNameHash1(dicomData.getLastNameHash1());
			uploadJob.setLastNameHash2(dicomData.getLastNameHash2());
			uploadJob.setLastNameHash3(dicomData.getLastNameHash3());
			uploadJob.setFirstNameHash1(dicomData.getFirstNameHash1());
			uploadJob.setFirstNameHash2(dicomData.getFirstNameHash2());
			uploadJob.setFirstNameHash3(dicomData.getFirstNameHash3());
			uploadJob.setBirthDateHash(dicomData.getBirthDateHash());
		}
		Date patientBirthDateFirstDayOfYear = getFirstDayOfTheYear(dicomData.getBirthDate());
		uploadJob.setPatientBirthDate(ShUpConfig.formatter.format(patientBirthDateFirstDayOfYear));
		uploadJob.setPatientSex(dicomData.getSex());
		/** 
		 * Study level
		 */
		uploadJob.setStudyInstanceUID(dicomData.getStudyInstanceUID());
		uploadJob.setStudyDate(ShUpConfig.formatter.format(dicomData.getStudyDate()));
		uploadJob.setStudyDescription(dicomData.getStudyDescription());
		/**
		 * Serie level
		 */
		uploadJob.setSeries(selectedSeries);
	}

	/**
	 * Initializes UploadStatusServiceJob object
	 * 
	 */
	private void initDataUploadJob(final Set<org.shanoir.dicom.importer.Serie> selectedSeries,
			final DicomDataTransferObject dicomData, NominativeDataUploadJob dataUploadJob) {
		dataUploadJob.setPatientName(dicomData.getFirstName() + " " + dicomData.getLastName());
		dataUploadJob.setPatientPseudonymusHash(dicomData.getSubjectIdentifier());
		dataUploadJob.setStudyDate(ShUpConfig.formatter.format(dicomData.getStudyDate()));
		dataUploadJob.setIPP(dicomData.getIPP());
		Serie firstSerie = selectedSeries.iterator().next();
		dataUploadJob.setMriSerialNumber(firstSerie.getMriInformation().getManufacturer()
				+ "(" + firstSerie.getMriInformation().getDeviceSerialNumber() + ")");
		dataUploadJob.setUploadPercentage("");
	}
	
	/**
	 * For OFSEP, do not transfer the real birth date but the first day of the year
	 *
	 * @return the date of the first day of the year
	 */
	private Date getFirstDayOfTheYear(Date pBirthDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("getFirstDayOfTheYear : Begin");
			logger.debug("getFirstDayOfTheYear : current subject birth date=" + pBirthDate);
		}
		if (pBirthDate != null) {
			final GregorianCalendar birthDate = new GregorianCalendar();
			birthDate.setTime(pBirthDate);
			// set day and month to 01/01
			birthDate.set(Calendar.MONTH, Calendar.JANUARY);
			birthDate.set(Calendar.DAY_OF_MONTH, 1);
			birthDate.set(Calendar.HOUR, 1);
			if (logger.isDebugEnabled()) {
				logger.debug("getFirstDayOfTheYear : anonymity birth date=" + birthDate.getTime());
				logger.debug("getFirstDayOfTheYear : End");
			}
			return birthDate.getTime();
		}
		logger.debug("getFirstDayOfTheYear : End - return null");
		return null;
	}

	@Override
	public String toString() {
		return "DownloadOrCopyRunnable [isFromPACS=" + isFromPACS + ", dicomServerClient=" + dicomServerClient
				+ ", filePathDicomDir=" + filePathDicomDir + ", selectedSeries=" + selectedSeries + ", dicomData="
				+ dicomData.toString() + "]";
	}

}
