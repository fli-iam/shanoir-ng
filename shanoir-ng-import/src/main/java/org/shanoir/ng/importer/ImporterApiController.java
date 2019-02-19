package org.shanoir.ng.importer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.List;

import javax.validation.Valid;

import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.ImportJobConstructorService;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.dicom.query.QueryPACSService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.ImportErrorModelCode;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ImportUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiParam;

/**
 * This is the main component of the import of Shanoir-NG.
 * The front-end in Angular only communicates with this service.
 * The import ms itself is calling the ms datasets service.
 * 
 * @author mkain
 *
 */
@Controller
public class ImporterApiController implements ImporterApi {

	private static final Logger LOG = LoggerFactory.getLogger(ImporterApiController.class);

	private static final SecureRandom RANDOM = new SecureRandom();
	
	private static final String FILE_POINT = ".";

	private static final String DICOMDIR = "DICOMDIR";
	
	private static final String IMPORTJOB = "importJob.json";

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String APPLICATION_ZIP = "application/zip";

	private static final String UPLOAD_FILE_SUFFIX = ".upload";

	private static final String ZIP_FILE_SUFFIX = ".zip";
	
	@Value("${shanoir.import.directory}")
	private String importDir;
	
	@Autowired
	private DicomDirToModelService dicomDirToModel;
	
	@Autowired
	private ImportJobConstructorService importJobConstructorService;
	
	@Autowired
	private ImagesCreatorAndDicomFileAnalyzerService imagesCreatorAndDicomFileAnalyzer;
	
	@Autowired
	private ImporterManagerService importerManagerService;
	
	@Autowired
	private QueryPACSService queryPACSService;
	
	public ResponseEntity<Void> uploadFiles(
			@ApiParam(value = "file detail") @RequestPart("files") MultipartFile[] files) throws RestServiceException {
		if (files.length == 0)
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded.", null));
		try {
			// not used currently
			for (int i = 0; i < files.length; i++) {
				saveTempFile(new File(importDir), files[i]);
			}
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		}
	}

	public ResponseEntity<ImportJob> uploadDicomZipFile(
			@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile)
			throws RestServiceException {
		if (dicomZipFile == null)
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded.", null));
		if (!isZipFile(dicomZipFile))
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Wrong content type of file upload, .zip required.", null));
		try {
			/**
			 * 1. STEP: Handle file management.
			 * Always create a userId specific folder in the import work folder (the root of everything):
			 * split imports to clearly separate them into separate folders for each user
			 */
			final Long userId = KeycloakUtil.getTokenUserId();
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs(); // create if not yet existing
			}
			File importJobDir = saveTempFileCreateFolderAndUnzip(userImportDir, dicomZipFile);
	
			/**
			 * 2. STEP: read DICOMDIR and create Shanoir model from it (== Dicom model):
			 * Patient - Study - Serie - Instance
			 */
			List<Patient> patients = null;
			File dicomDirFile = new File(importJobDir.getAbsolutePath() + File.separator + DICOMDIR);
			if (dicomDirFile.exists()) {
				patients = dicomDirToModel.readDicomDirToPatients(dicomDirFile);
			}
			/**
			 * 3. STEP: split instances into non-images and images and get additional meta-data
			 * from first dicom file of each serie, meta-data missing in dicomdir.
			 */
			imagesCreatorAndDicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, importJobDir.getAbsolutePath(), false);
	
			/**
			 * 4. STEP: create ImportJob
			 */
			ImportJob importJob = new ImportJob();
			importJob.setFromDicomZip(true);
			// Work folder is always relative to general import directory and userId (not shown to outside world)
			importJob.setWorkFolder(File.separator + importJobDir.getName());
			importJob.setPatients(patients);
			return new ResponseEntity<ImportJob>(importJob, HttpStatus.OK);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		} catch (ShanoirException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		}
	}

	@Override
	public ResponseEntity<Void> uploadDicomZipFileFromShup(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile)
			throws RestServiceException, ShanoirException {
		if (dicomZipFile == null)
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded.", null));
		try {
			final Long userId = KeycloakUtil.getTokenUserId();
			final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
			final File userImportDir = new File(userImportDirFilePath);
			if (!userImportDir.exists()) {
				userImportDir.mkdirs(); // create if not yet existing
			}
			File importJobDir = saveTempFileCreateFolderAndUnzip(userImportDir, dicomZipFile);
			File importJobFile = new File(importJobDir.getAbsolutePath() + File.separator + IMPORTJOB);
			ImportJob importJob = null;
			if (importJobFile.exists()) {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					importJob = objectMapper.readValue(importJobFile, ImportJob.class);
					importJob = importJobConstructorService.reconstructImportJob(importJob, importJobDir);
					LOG.warn(objectMapper.writeValueAsString(importJob));
				} catch (IOException ioe) {
					LOG.error(ioe.getMessage(), ioe);
					throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"Error while mapping importJob.json file to object.", null));
				}
			} else {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
						"Error missing importJob.json in upload from ShUp.", null));				
			}
			importJob.setFromShanoirUploader(true);
			importJob.setWorkFolder(File.separator + importJobDir.getName());
			importerManagerService.manageImportJob(userId, KeycloakUtil.getKeycloakHeader(), importJob);
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		} catch (RestClientException e) {
			LOG.error("Error on dataset microservice request", e);
			throw new ShanoirException("Error while sending import job", ImportErrorModelCode.SC_MS_COMM_FAILURE);
		} catch (ShanoirException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Authentication issue.", null));
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> startImportJob( @ApiParam(value = "ImportJob", required = true) @Valid @RequestBody final ImportJob importJob)
			throws RestServiceException {
		try {
			final Long userId = KeycloakUtil.getTokenUserId();
			importerManagerService.manageImportJob(userId, KeycloakUtil.getKeycloakHeader(), importJob);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					e.getMessage(), null));
		}
	}
	
	@Override
	public ResponseEntity<ImportJob> queryPACS( @ApiParam(value = "DicomQuery", required = true) @Valid @RequestBody final DicomQuery dicomQuery)
			throws RestServiceException {
		ImportJob importJob;
		try {
			importJob = queryPACSService.queryCFIND(dicomQuery);
			// the pacs workfolder is empty here, as multiple queries could be asked before string an import
			importJob.setWorkFolder("");
			importJob.setFromPacs(true);
		} catch (ShanoirException e) {
			LOG.error(e.getMessage(), e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
							e.getMessage(), null));
		}
		if (importJob.getPatients() == null || importJob.getPatients().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<ImportJob>(importJob, HttpStatus.OK);
	}

	/**
	 * This method takes a multipart file and stores it in a configured upload
	 * directory in relation with the userId with a random name and the suffix .upload
	 *
	 * @param file
	 * @throws IOException
	 */
	private File saveTempFile(final File userImportDir, final MultipartFile file) throws IOException {
		long n = createRandomLong();
		File uploadFile = new File(userImportDir.getAbsolutePath(), Long.toString(n) + UPLOAD_FILE_SUFFIX);
		byte[] bytes = file.getBytes();
		Files.write(uploadFile.toPath(), bytes);
		return uploadFile;
	}
	
	/**
	 * This method creates a random long number.
	 * 
	 * @return long: random number
	 */
	private long createRandomLong() {
		long n = RANDOM.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}
		return n;
	}

	/**
	 * This method stores an uploaded zip file in a temporary file, creates a new folder with the same
	 * name and unzips the content into this folder, and gives back the folder with the content.
	 * 
	 * @param userImportDir
	 * @param dicomZipFile
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	private File saveTempFileCreateFolderAndUnzip(final File userImportDir, final MultipartFile dicomZipFile) throws IOException, RestServiceException {
		File tempFile = saveTempFile(userImportDir, dicomZipFile);
		if (!ImportUtils.checkZipContainsFile(DICOMDIR, tempFile))
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"DICOMDIR is missing in .zip file.", null));
		String fileName = tempFile.getName();
		int pos = fileName.lastIndexOf(FILE_POINT);
		if (pos > 0) {
			fileName = fileName.substring(0, pos);
		}
		File unzipFolderFile = new File(tempFile.getParentFile().getAbsolutePath() + File.separator + fileName);
		if (!unzipFolderFile.exists()) {
			unzipFolderFile.mkdirs();
		} else {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while unzipping file: folder already exists.", null));
		}
		ImportUtils.unzip(tempFile.getAbsolutePath(), unzipFolderFile.getAbsolutePath());
		return unzipFolderFile;
	}

	/**
	 * Check if sent file is of type .zip.
	 *
	 * @param file
	 */
	private boolean isZipFile(MultipartFile file) {
		if (file.getContentType().equals(APPLICATION_ZIP) || file.getContentType().equals(APPLICATION_OCTET_STREAM)
				|| file.getOriginalFilename().endsWith(ZIP_FILE_SUFFIX)) {
			return true;
		}
		return false;
	}

}
