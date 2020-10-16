/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BrukerApiController implements BrukerApi {

	private static final String ERROR_BRUKER2DICOM_REQUEST = "Error on bruker2dicom microservice request";

	private static final Logger LOG = LoggerFactory.getLogger(BrukerApiController.class);

	public static final String BRUKER_FOLDER = "bruker";
	public static final String CONVERT_FOLDER = "convert";
	public static final String FOLDER_SEP = "/";
	public static final String TEMP_FOLDER = "/tmp";
	public static final String RECONSTRUCTED_DATA_FILES = "2dseq";
	public static final String ZIP_EXTENSION = ".zip";
	public static final String RESULT_FOLDER = "result";

	public static final int KB = 1024;
	public static final int BUFFER_SIZE = 2 * KB;

	@Value("${ms.url.bruker2dicom}")
	private String bruker2DicomMsUrl;

	@Autowired
	private ShanoirPreclinicalConfiguration preclinicalConfig;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public ResponseEntity<String> uploadBrukerFile(@RequestParam("files") MultipartFile[] uploadfiles)
			throws RestServiceException {
		if (uploadfiles == null || uploadfiles.length == 0) {
			LOG.error("uploadFiles is null or empty");
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded", null));
		}
		Path brukerDirFile = null;
		try {
			MultipartFile brukerFile = uploadfiles[0];
			String fileName = brukerFile.getOriginalFilename();
			int id = fileName.toLowerCase().indexOf(ZIP_EXTENSION);
			if (id != -1) {
				fileName = fileName.substring(0, id);
			}
			LOG.info("upload bruker file [{}]", fileName);
			brukerDirFile = createBrukerTempFile(fileName);
			String brukerDirString = brukerDirFile.getFileName().toString();

			LOG.info("bruker temp file has been created {}", brukerDirFile.toFile().getAbsolutePath());
			Path uploadedFile = saveUploadedFileTmp(brukerFile, brukerDirFile);
			LOG.info("bruker zip archive has been uploaded ");
			unzipBrukerArchive(uploadedFile.toFile().getAbsolutePath(), brukerDirFile.toFile().getAbsolutePath());
			LOG.info("bruker file has been unzipped into {}", brukerDirFile);
			boolean isValidBruker = checkBrukerCrawlFor2dseq(
					Arrays.asList(new File(brukerDirFile.toAbsolutePath().toString()).listFiles()),
					RECONSTRUCTED_DATA_FILES);
			LOG.info("isValidBruker for {{}}? {}", fileName, isValidBruker);

			String destinationFilePath = brukerDirFile.toAbsolutePath().toString() + File.separator + RESULT_FOLDER;
			if (isValidBruker) {
				LOG.info("START BRUKER 2 DICOM");
				startBruker2Dicom(brukerDirFile);
				LOG.info("ZIP DICOM FILES");
				File destinationZip = File.createTempFile(destinationFilePath, "." + brukerDirString + ".converted.zip");
				LOG.info("zip destinationFilePath = {}, destinationZip = {}, rootFolderToRemove = {}", destinationFilePath, destinationZip.getAbsolutePath(), RESULT_FOLDER);
				zipFolder(destinationFilePath, destinationZip, RESULT_FOLDER);
				return new ResponseEntity<>(destinationZip.getAbsolutePath(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(destinationFilePath, HttpStatus.NOT_ACCEPTABLE);
			}
		} catch (IOException e) {
			LOG.error("Error while uploadBrukerFile: issue with file {}", e.getMessage(), e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file", null));
		} catch (Exception e) {
			LOG.error("Error while zipping dicom files: {}", e.getMessage(), e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file", null));
		} finally {
			if (brukerDirFile != null) {
				FileUtils.deleteQuietly(brukerDirFile.toFile());
			}
		}
	}

	/**
	 * This method does a REST http post call to the docker container bruker2dicom
	 * to start the conversion of bruker2dicom.
	 * 
	 * @param brukerDirFile
	 * @throws RestServiceException
	 */
	private void startBruker2Dicom(Path brukerDirFile) throws RestServiceException {
		String sourceFilePath = brukerDirFile.toAbsolutePath().toString();
		String destinationFilePath = brukerDirFile.toAbsolutePath().toString() + File.separator + RESULT_FOLDER;
		String requestJson = "{\"source\":\"" + sourceFilePath
				+ "\", \"destination\":\"" + destinationFilePath
				+ "\", \"dicomdir\": true }";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// HttpEntity represents the request
		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

		// Post to Bruker2Dicom to start conversion
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(bruker2DicomMsUrl, HttpMethod.POST, entity, String.class);
		} catch (RestClientException e) {
			LOG.error(ERROR_BRUKER2DICOM_REQUEST, e);
			throw new RestServiceException(e, new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while converting bruker2dicom.", null));
		} catch (Exception e2) {
			LOG.error(ERROR_BRUKER2DICOM_REQUEST, e2);
			throw new RestServiceException(e2, new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while converting bruker2dicom.", null));
		}

		if (!HttpStatus.OK.equals(response.getStatusCode()) && !HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
			LOG.error(ERROR_BRUKER2DICOM_REQUEST);
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
		int index = 0;
		String pathFile = preclinicalConfig.getUploadBrukerFolder() + BRUKER_FOLDER + FOLDER_SEP + CONVERT_FOLDER
				+ FOLDER_SEP + fileName;
		// if the folder exists, create a new folder by adding a figure at the end of
		// the folder name
		Path path = Paths.get(pathFile + FOLDER_SEP + String.valueOf(index));
		while (path.toFile().exists()) {
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
		File destFile = pathToFile.toFile();
		brukerFile.transferTo(destFile);
		return pathToFile;
	}

	/**
	 * unzip a zip file
	 * 
	 * @param brukerPath
	 * @param brukerDirPath
	 * @throws IOException
	 */
	private void unzipBrukerArchive(String brukerPath, String brukerDirPath) throws IOException {
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(brukerPath))) {
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
		}
	}

	/**
	 * extract file from a zip
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		}
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

	private static void zipFolder(String srcFolder, File destZipFile, String rootFolderToRemove) throws Exception {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;

		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);

		addFolderToZip("", srcFolder, zip, rootFolderToRemove);
		zip.flush();
		zip.close();
	}

	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip, String rootFolderToRemove)
			throws Exception {
		File folder = new File(srcFolder);
		if (folder.list() != null) {
			for (String fileName : folder.list()) {
				if (path.equals("")) {
					addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, rootFolderToRemove);
				} else {
					addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, rootFolderToRemove);
				}
			}
		}
	}

	private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, String rootFolderToRemove)
			throws Exception {

		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip, rootFolderToRemove);
		} else {
			byte[] buf = new byte[1024];
			int len;
			try (FileInputStream in = new FileInputStream(srcFile)) {
				String pathAfterOmittingtheRootFolder = path.replaceFirst(rootFolderToRemove, "");
				zip.putNextEntry(new ZipEntry(pathAfterOmittingtheRootFolder + "/" + folder.getName()));
	
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			}
		}
	}

}
