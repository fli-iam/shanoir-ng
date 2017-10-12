package org.shanoir.ng.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;

import org.shanoir.ng.importer.dicom.DicomDirToJsonReader;
import org.shanoir.ng.importer.dicom.DicomFileAnalyzer;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-08-10T08:45:26.334Z")

@Controller
public class ImporterApiController implements ImporterApi {

	private static final Logger LOG = LoggerFactory.getLogger(ImporterApiController.class);

	private static final String FILE_POINT = ".";

	private static final String DICOMDIR = "DICOMDIR";

	private static final String ZIP = ".zip";

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String APPLICATION_ZIP = "application/zip";

	private static final SecureRandom random = new SecureRandom();

	private static final String UPLOAD_FILE_SUFFIX = ".upload";

	@Value("${shanoir.import.upload.folder}")
    private String uploadFolder;
	
    public ResponseEntity<Void> uploadFiles(@ApiParam(value = "file detail") @RequestPart("files") MultipartFile[] files) throws RestServiceException {
    		if(files.length == 0) throw new RestServiceException(
    				new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded.", null));
		try {
			for (int i = 0; i < files.length; i++) {
				saveTempFile(files[i]);
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		}
    }

    /**
     * This method takes a multipart file and stores it in a configured
     * upload folder with a random name and the suffix .upload
     * @param file
     * @throws IOException
     */
	private File saveTempFile(MultipartFile file) throws IOException {
		long n = random.nextLong();
		if (n == Long.MIN_VALUE) {
		    n = 0;      // corner case
		} else {
		    n = Math.abs(n);
		}
		File uploadFile = new File(uploadFolder, Long.toString(n) + UPLOAD_FILE_SUFFIX);
		byte[] bytes = file.getBytes();
		Files.write(uploadFile.toPath(), bytes);
		return uploadFile;
	}

	/**
	 * @todo refactor and clean-up here
	 */
    public ResponseEntity<String> uploadDicomZipFile(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile) throws RestServiceException {
		if(dicomZipFile == null) throw new RestServiceException(
				new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded.", null));
		if(!isZipFile(dicomZipFile)) throw new RestServiceException(
				new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Wrong content type of file upload, .zip required.", null));

		try {
			File tempFile = saveTempFile(dicomZipFile);
			if(!Utils.checkZipContainsFile(DICOMDIR, tempFile)) throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "DICOMDIR is missing in .zip file.", null));
			
			String fileName = tempFile.getName();
			int pos = fileName.lastIndexOf(FILE_POINT);
			if (pos > 0) {
			    fileName = fileName.substring(0, pos);
			}
			
			File unzipFolderFile = new File(tempFile.getParentFile().getAbsolutePath() + File.separator + fileName);
			if(!unzipFolderFile.exists()) {
				unzipFolderFile.mkdirs();
			} else {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while unzipping file: folder already exists.", null));
			}
			
			Utils.unzip(tempFile.getAbsolutePath(), unzipFolderFile.getAbsolutePath());
			
			File dicomDirFile = new File(unzipFolderFile.getAbsolutePath() + File.separator + DICOMDIR);
			DicomDirToJsonReader dicomDirToJsonReader = null;
			JsonNode dicomDirJsonNode = null;
			if(dicomDirFile.exists()) {
				dicomDirToJsonReader = new DicomDirToJsonReader(dicomDirFile);
				dicomDirJsonNode = dicomDirToJsonReader.readDicomDirToJsonNode();
			}
			
			DicomFileAnalyzer dicomFileAnalyzer = new DicomFileAnalyzer(dicomDirJsonNode);
			dicomFileAnalyzer.analyzeDicomFiles();
			
			String dicomDirJsonString = dicomDirToJsonReader.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dicomDirJsonNode);
//			LOG.info(dicomDirJsonString);
			return new ResponseEntity<String>(dicomDirJsonString, HttpStatus.OK);
			
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		}
    }

    /**
     * Check if sent file is of type .zip.
     * @param file
     */
	private boolean isZipFile(MultipartFile file) {
		if(file.getContentType().equals(APPLICATION_ZIP)
				|| file.getContentType().equals(APPLICATION_OCTET_STREAM)
				|| file.getOriginalFilename().endsWith(ZIP)) {
			return true;
		}
		return false;
	}
}




















