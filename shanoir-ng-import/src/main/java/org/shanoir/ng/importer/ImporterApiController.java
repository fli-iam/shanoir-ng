package org.shanoir.ng.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.shanoir.anonymization.anonymization.AnonymizationServiceImpl;
import org.shanoir.ng.importer.dcm2nii.NIfTIConverterService;
import org.shanoir.ng.importer.dicom.DicomDirToJsonReaderService;
import org.shanoir.ng.importer.dicom.DicomFileAnalyzerService;
import org.shanoir.ng.importer.dicom.ImportJobConstructorService;
import org.shanoir.ng.importer.model.Image;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Patients;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
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

	private static final String FILE_POINT = ".";

	private static final String DICOMDIR = "DICOMDIR";
	
	private static final String IMPORTJOB = "importJob.json";

	private static final String ZIP = ".zip";

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String APPLICATION_ZIP = "application/zip";

	private static final SecureRandom RANDOM = new SecureRandom();

	private static final String UPLOAD_FILE_SUFFIX = ".upload";

	@Value("${shanoir.import.upload.folder}")
	private String uploadFolder;
	
	@Value("${ms.url.shanoir-ng-datasets}")
	private String datasetsMsUrl;

	@Autowired
	private DicomDirToJsonReaderService dicomDirToJsonReader;
	
	@Autowired
	private ImportJobConstructorService importJobConstructorService;
	
	@Autowired
	private DicomFileAnalyzerService dicomFileAnalyzer;

	@Autowired
	private NIfTIConverterService niftiConverter;
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * For the moment Spring is not used here to autowire, as we could keep the
	 * anonymization project as simple as it is, without Spring annotations.
	 * Maybe to change and think about deeper afterwards.
	 */
	private AnonymizationServiceImpl anonymizer = new AnonymizationServiceImpl();

	public ResponseEntity<Void> uploadFiles(
			@ApiParam(value = "file detail") @RequestPart("files") MultipartFile[] files) throws RestServiceException {
		if (files.length == 0)
			throw new RestServiceException(
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

	@Override
	public ResponseEntity<Void> startImportJob( @ApiParam(value = "Importjob", required = true) @Valid @RequestBody final ImportJob importJob)
			throws RestServiceException {
		try {
			LOG.info("start import job: " + importJob.toString());
			
			File workFolder = new File(importJob.getWorkFolder());
			List<Patient> patients = importJob.getPatients();
			for (Iterator patientsIt = patients.iterator(); patientsIt.hasNext();) {
				Patient patient = (Patient) patientsIt.next();
				ArrayList<File> dicomFiles = getDicomFilesForPatient(patient, workFolder);
//				anonymizer.anonymizeForShanoir(dicomFiles, "Neurinfo Profile", patient.getPatientName(), patient.getPatientID());
				Long converterId = importJob.getFrontConverterId();
				niftiConverter.prepareAndRunConversion(patient, workFolder, converterId);
			}
			
			String importJobJsonString = dicomDirToJsonReader.getMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(importJob);
			LOG.info(importJobJsonString);
			
			// HttpEntity represents the request
			final HttpEntity<ImportJob> requestBody = new HttpEntity<>(importJob, KeycloakUtil.getKeycloakHeader());

			// Post to dataset MS to finish import
			ResponseEntity<String> response = null;
			try {
				response = restTemplate.exchange(datasetsMsUrl, HttpMethod.POST, requestBody, String.class);
			} catch (RestClientException e) {
				LOG.error("Error on dataset microservice request", e);
				throw new ShanoirException("Error while sending import job", ImportErrorModelCode.SC_MS_COMM_FAILURE);
			}

			if (HttpStatus.OK.equals(response.getStatusCode())
					|| HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
			} else {
				throw new ShanoirException(ImportErrorModelCode.SC_MS_COMM_FAILURE);
			}
			
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					e.getMessage(), null));
		}
	}

	/**
	 * Using Java HashSet here to avoid duplicate files for anonymization.
	 * For performance reasons already init with 2000 buckets, assuming,
	 * that we will normally never have more than 2000 files to process.
	 * Maybe to be evaluated later with more bigger imports.
	 * @param importJob
	 * @throws FileNotFoundException 
	 */
	private ArrayList<File> getDicomFilesForPatient(final Patient patient, final File workFolder) throws FileNotFoundException {
		Set<File> pathsSet = new HashSet<File>(2000);
		List<Study> studies = patient.getStudies();
		for (Iterator studiesIt = studies.iterator(); studiesIt.hasNext();) {
			Study study = (Study) studiesIt.next();
			List<Serie> series = study.getSeries();
			for (Iterator seriesIt = series.iterator(); seriesIt.hasNext();) {
				Serie serie = (Serie) seriesIt.next();
				List<Image> images = serie.getImages();
				for (Iterator imagesIt = images.iterator(); imagesIt.hasNext();) {
					Image image = (Image) imagesIt.next();
					String path = image.getPath();
					File file = new File(workFolder.getAbsolutePath() + File.separator + path);
					if(file.exists()) {
						pathsSet.add(file);
					} else {
						throw new FileNotFoundException("File not found: " + path);
					}
				}
			}
		}
		return new ArrayList<File>(pathsSet);
	}

	/**
	 * This method takes a multipart file and stores it in a configured upload
	 * folder with a random name and the suffix .upload
	 *
	 * @param file
	 * @throws IOException
	 */
	private File saveTempFile(MultipartFile file) throws IOException {
		long n = RANDOM.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
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
			File tempFile = saveTempFile(dicomZipFile);
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

			File dicomDirFile = new File(unzipFolderFile.getAbsolutePath() + File.separator + DICOMDIR);
			JsonNode dicomDirJsonNode = null;
			if (dicomDirFile.exists()) {
				dicomDirJsonNode = dicomDirToJsonReader.readDicomDirToJsonNode(dicomDirFile);
			}

			dicomFileAnalyzer.analyzeDicomFiles(dicomDirJsonNode, unzipFolderFile.getAbsolutePath());

			String dicomDirJsonString = dicomDirToJsonReader.getMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(dicomDirJsonNode);
			LOG.info(dicomDirJsonString);
			Patients patientsDTO = dicomDirToJsonReader.getMapper().readValue(dicomDirJsonString, Patients.class);
			ImportJob importJob = new ImportJob();
			importJob.setWorkFolder(unzipFolderFile.getAbsolutePath());
			importJob.setPatients(patientsDTO.getPatients());
			return new ResponseEntity<ImportJob>(importJob, HttpStatus.OK);
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		}
	}

	/**
	 * Check if sent file is of type .zip.
	 *
	 * @param file
	 */
	private boolean isZipFile(MultipartFile file) {
		if (file.getContentType().equals(APPLICATION_ZIP) || file.getContentType().equals(APPLICATION_OCTET_STREAM)
				|| file.getOriginalFilename().endsWith(ZIP)) {
			return true;
		}
		return false;
	}

	@Override
	public ResponseEntity<Void> uploadDicomZipFileFromShup(@ApiParam(value = "file detail") @RequestPart("file") MultipartFile dicomZipFile)
			throws RestServiceException, ShanoirException {
		// TODO Auto-generated method stub
		if (dicomZipFile == null)
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded.", null));

		try {
			File tempFile = saveTempFile(dicomZipFile);

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

			
			File importJobFile = new File(unzipFolderFile.getAbsolutePath() + File.separator + IMPORTJOB);
			ImportJob importJob = null;
			if (importJobFile.exists()) {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					importJob = objectMapper.readValue(importJobFile, ImportJob.class);
					importJob = importJobConstructorService.reconstructImportJob(importJob,unzipFolderFile);
					LOG.warn(objectMapper.writeValueAsString(importJob));
				} catch (IOException ioe) {
					LOG.error(ioe.getMessage(),ioe);
					throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
							"Error while mapping importJob.json file to object.", null));
				}
			}

			File workFolder = new File(importJob.getWorkFolder());
			List<Patient> patients = importJob.getPatients();
			for (Iterator patientsIt = patients.iterator(); patientsIt.hasNext();) {
				Patient patient = (Patient) patientsIt.next();
				ArrayList<File> dicomFiles = getDicomFilesForPatient(patient, workFolder);
				Long converterId = importJob.getFrontConverterId();
				niftiConverter.prepareAndRunConversion(patient, workFolder, converterId);
			}
			
			String importJobJsonString = dicomDirToJsonReader.getMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(importJob);
			LOG.info(importJobJsonString);
			
			// HttpEntity represents the request
			final HttpEntity<ImportJob> requestBody = new HttpEntity<>(importJob, KeycloakUtil.getKeycloakHeader());

			// Post to dataset MS to finish import
			ResponseEntity<String> response = null;
				response = restTemplate.exchange(datasetsMsUrl, HttpMethod.POST, requestBody, String.class);

			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file.", null));
		} catch (RestClientException e) {
			LOG.error("Error on dataset microservice request", e);
			throw new ShanoirException("Error while sending import job", ImportErrorModelCode.SC_MS_COMM_FAILURE);
		} catch (ShanoirException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Authentication issue.", null));
		}
		
	}

}
