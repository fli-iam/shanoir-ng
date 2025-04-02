package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.importer.dicom.DicomSerieAndInstanceAnalyzer;
import org.shanoir.ng.importer.dicom.InstanceNumberSorter;
import org.shanoir.ng.importer.model.Instance;
import org.shanoir.ng.importer.model.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class introduced to perform
 * optimized operations in Java within the file system.
 * @author mkain
 *
 */
public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	/**
	 * This method receives a directory name as a string
	 * and checks if there already exists a file like this.
	 * If the file doesn't exist a new directory is created.
	 * @param dirName
	 * @return true - if the directory exists, false - otherwise
	 */
	public static void dirExistsWithCreate(String dirName) {
		final File folder = new File(dirName);
		if (!folder.exists()) {
			if(!folder.mkdir()) {
				logger.error("dirExistsWithCreate: error while creating directory " + dirName);
			}
		}
	}
	
	/**
	 * Use this method for calculating the file size.
	 * @param size
	 * @return
	 */
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 * Java NIO file copying to improve the performance of file copying.
	 * In general this solution requires only 2/3 of the time of standard
	 * Java IO. The difference of time is growing regarding bigger files,
	 * starting with 5 MB up to x GB.
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) {
		FileChannel source = null;
		FileChannel destination = null;
		try {
			if (!destFile.exists()) {
				destFile.createNewFile();
			}
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (FileNotFoundException e) {
			logger.error("File " +sourceFile.getAbsolutePath()+" not found",e);
		} catch (IOException e) {
			logger.error("IO Exception" , e);
		} finally {
			try {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			} catch (IOException e) {
				logger.error("IO Exception" , e);
			}
		}
	}

	// public static void deleteFolder(File folder) {
	// 	if (folder.isDirectory()) {
	// 		for (File file : folder.listFiles()) {
	// 			deleteFolder(file);
	// 		}
	// 	}
	// 	if (!folder.delete()) {
	// 		logger.error("Error deleting file: " + folder.getAbsolutePath());
	// 	}
	// }

	public static void cleanTempFolders(File workFolder, String studyInstanceUID) {
		File tempStudyInstanceUIDFolder = new File(workFolder, studyInstanceUID);
		if (tempStudyInstanceUIDFolder.exists()) {
			tempStudyInstanceUIDFolder.delete();
			logger.info("Temp folder of last download found and cleaned: " + tempStudyInstanceUIDFolder.getAbsolutePath());
		}
	}

	public static void readAndCopyDicomFilesToUploadFolder(File workFolder, String studyInstanceUID, Set<Serie> selectedSeries, final File uploadFolder,
			final List<String> retrievedDicomFiles, StringBuilder downloadOrCopyReport) throws IOException {
		for (Serie serie : selectedSeries) {
			List<String> fileNamesForSerie = new ArrayList<String>();
			final String seriesInstanceUID = serie.getSeriesInstanceUID();
			File serieFolder = new File(workFolder
				+ File.separator + studyInstanceUID
				+ File.separator + seriesInstanceUID);
			if (serieFolder.exists()) {
				List<Instance> instances = new ArrayList<>();
				File[] serieFiles = serieFolder.listFiles();
				for (int i = 0; i < serieFiles.length; i++) {
					String dicomFileName = serieFiles[i].getName();
					fileNamesForSerie.add(dicomFileName);
					File sourceFileFromPacs = serieFiles[i];
					try (DicomInputStream dIS = new DicomInputStream(sourceFileFromPacs)) { // keep try to finally close input stream
						Attributes attributes = dIS.readDataset();
						if (!DicomSerieAndInstanceAnalyzer.checkInstanceIsIgnored(attributes)) {
							Instance instance = new Instance(attributes);
							instances.add(instance);						
							File destSerieFolder = new File(uploadFolder.getAbsolutePath() + File.separator + seriesInstanceUID);
							if (!destSerieFolder.exists())
								destSerieFolder.mkdirs();
							File destDicomFile = new File(destSerieFolder, dicomFileName);
							Files.copy(sourceFileFromPacs.toPath(), destDicomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							sourceFileFromPacs.delete();
						}
					}
				}
				if (!instances.isEmpty()) {
					instances.sort(new InstanceNumberSorter());
					serie.setInstances(instances);
					logger.info(instances.size() + " instances found for serie " + serie.getSeriesDescription());
				} else {
					logger.warn("Serie found with empty instances and therefore ignored (SeriesDescription: {}, SerieInstanceUID: {}).", serie.getSeriesDescription(), serie.getSeriesInstanceUID());
					serie.setIgnored(true);
					serie.setSelected(false);
				}
				downloadOrCopyReport.append("Download: serie "
					+ (serie.getSeriesNumber() != null ? "(No. " + serie.getSeriesNumber() + ") " : "")
					+ serie.getSeriesDescription()
					+ " downloaded with " + fileNamesForSerie.size() + " images.\n");
				if (serie.getInstances().size() != fileNamesForSerie.size()) {
					downloadOrCopyReport.append("Error: Download: serie "
						+ (serie.getSeriesNumber() != null ? "(No. " + serie.getSeriesNumber() + ") " : "")
						+ serie.getSeriesDescription()
						+ " downloaded with " + fileNamesForSerie.size()
						+ " images not equal to instances in the DICOM server: " + serie.getInstances().size() + ".\n");
				}
				if (serie.getNumberOfSeriesRelatedInstances() != null
					&& serie.getNumberOfSeriesRelatedInstances().intValue() != 0
					&& serie.getNumberOfSeriesRelatedInstances().intValue() != serie.getInstances().size()) {
					logger.warn("Download: serie "
						+ (serie.getSeriesNumber() != null ? "(No. " + serie.getSeriesNumber() + ") " : "")
						+ serie.getSeriesDescription()
						+ " getNumberOfSeriesRelatedInstances (" + serie.getNumberOfSeriesRelatedInstances().intValue()
						+ ") != " + serie.getInstances().size()
					);
				}
				retrievedDicomFiles.addAll(fileNamesForSerie);
				logger.info(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
						+ " DICOM files for serie " + seriesInstanceUID + ": " + serie.getSeriesDescription()
						+ " was successful.\n\n");
			} else {
				downloadOrCopyReport.append("Error: Download: serie "
					+ (serie.getSeriesNumber() != null ? "(No. " + serie.getSeriesNumber() + ") " : "")
				 	+ serie.getSeriesDescription() + " downloaded without an existing serie folder.\n");
				logger.error(uploadFolder.getName() + ":\n\n Download of " + fileNamesForSerie.size()
						+ " DICOM files for serie " + seriesInstanceUID + ": " + serie.getSeriesDescription()
						+ " has failed.\n\n");
			}
		}
	}

	public static void deleteFolderDownloadFromDicomServer(File workFolder, String studyInstanceUID, Set<Serie> selectedSeries) throws IOException {
		if (selectedSeries != null && !selectedSeries.isEmpty()) {
			File studyFolder = new File(workFolder + File.separator + studyInstanceUID);
			try (Stream<Path> walk = Files.walk(studyFolder.toPath())) {
				walk.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
			}
		}
	}

}
