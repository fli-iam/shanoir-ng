package org.shanoir.ng.preclinical.bruker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BrukerApiController implements BrukerApi {

	private static final Logger LOG = LoggerFactory.getLogger(BrukerApiController.class);

	public final static String BRUKER_FOLDER = "bruker";
	public final static String CONVERT_FOLDER = "convert";
	public final static String FOLDER_SEP = "/";
	public final static String TEMP_FOLDER = "/tmp";
	public final static String RECONSTRUCTED_DATA_FILES = "2dseq";
	public final static String ZIP_EXTENSION = ".zip";

	public static final int KB = 1024;
	public static final int BUFFER_SIZE = 2 * KB;

	@Autowired
	private ShanoirPreclinicalConfiguration preclinicalConfig;

	public ResponseEntity<String> uploadBrukerFile(@RequestParam("files") MultipartFile[] uploadfiles)
			throws RestServiceException {
		if (uploadfiles == null || uploadfiles.length == 0) {
			LOG.error("uploadFiles is null or empty " + (uploadfiles == null ? "null" : "empty"));
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded", null));

		}
		try {
			MultipartFile brukerFile = uploadfiles[0];
			String fileName = brukerFile.getOriginalFilename();
			int id = fileName.toLowerCase().indexOf(ZIP_EXTENSION);
			if (id != -1) {
				fileName = fileName.substring(0, id);
			}
			LOG.info("upload bruker file [" + fileName + "]");
			Path brukerDirFile = createBrukerTempFile(fileName);
			LOG.info("bruker temp file has been created " + brukerDirFile.toFile().getAbsolutePath());
			Path uploadedFile = saveUploadedFileTmp(brukerFile, brukerDirFile);
			LOG.info("bruker zip archive has been uploaded ");
			unzipBrukerArchive(uploadedFile.toFile().getAbsolutePath(), brukerDirFile.toFile().getAbsolutePath());
			LOG.info("bruker file has been unzipped into " + brukerDirFile);
			boolean isValidBruker = checkBrukerCrawlFor2dseq(
					Arrays.asList((new File(brukerDirFile.toAbsolutePath().toString())).listFiles()),
					RECONSTRUCTED_DATA_FILES);
			LOG.info("isValidBruker for {" + fileName + "}? " + isValidBruker);
			if (isValidBruker) {
				return new ResponseEntity<String>(brukerDirFile.toFile().getAbsolutePath(), HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(brukerDirFile.toFile().getAbsolutePath(), HttpStatus.NOT_ACCEPTABLE);
			}
		} catch (IOException e) {
			LOG.error("Error while uploadBrukerFile: issue with file " + (e == null ? "" : e.getMessage()), e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file", null));
		}
	}

	/**
	 * create temp folder file to upload bruker zip file
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private Path createBrukerTempFile(String fileName) throws IOException {
		String pathFile = preclinicalConfig.getUploadBrukerFolder() + BRUKER_FOLDER + FOLDER_SEP + CONVERT_FOLDER
				+ FOLDER_SEP + fileName;
		Path path = Paths.get(pathFile);
		// if the folder exists, create a new folder by adding a figure at the end of
		// the folder name
		int index = 0;
		while (Files.exists(path)) {
			path = Paths.get(pathFile + FOLDER_SEP + String.valueOf(index));
			index++;
		}
		Files.createDirectories(path);
		return path;
	}

	/**
	 * save zip bruker file into the tmp folder
	 * 
	 * @param brukerFile
	 * @param brukerDirPath
	 * @return
	 * @throws IOException
	 */
	private Path saveUploadedFileTmp(MultipartFile brukerFile, Path brukerDirPath) throws IOException {
		// Path to file
		Path pathToFile = Paths.get(brukerDirPath.toString() + FOLDER_SEP + brukerFile.getOriginalFilename());
		byte[] bytes = brukerFile.getBytes();
		return Files.write(pathToFile, bytes);
	}

	/**
	 * unzip a zip file
	 * 
	 * @param brukerPath
	 * @param brukerDirPath
	 * @throws IOException
	 */
	private void unzipBrukerArchive(String brukerPath, String brukerDirPath) throws IOException {
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(brukerPath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = brukerDirPath + FOLDER_SEP + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	/**
	 * extract file from a zip
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	/**
	 * check if there is reconstructed data file into a given list of files
	 * 
	 * @param foldersList
	 * @param fileNameToFind
	 * @return
	 */
	private boolean checkBrukerCrawlFor2dseq(List<File> foldersList, String fileNameToFind) {
		boolean found = false;
		for (File current : foldersList) {
			if (current.isDirectory() && !found) {
				found = checkBrukerCrawlFor2dseq(Arrays.asList(current.listFiles()), fileNameToFind);
			} else if (current.getName().equals(fileNameToFind)) {
				found = true;
				break;
			}
		}
		return found;
	}

}
