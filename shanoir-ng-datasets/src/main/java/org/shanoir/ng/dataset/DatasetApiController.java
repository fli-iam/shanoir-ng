package org.shanoir.ng.dataset;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.DatasetsErrorModelCode;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetApiController implements DatasetApi {

	private static final String ZIP = ".zip";

	private static final String DOWNLOAD = ".download";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final Logger LOG = LoggerFactory.getLogger(DatasetApiController.class);

	@Autowired
	private DatasetMapper datasetMapper;

	@Autowired
	private MrDatasetMapper mrDatasetMapper;
	
	@Autowired
	private DatasetService<Dataset> datasetService;

    private final HttpServletRequest request;
    
	@Autowired
	private WADODownloaderService downloader;

	private static final SecureRandom RANDOM = new SecureRandom();
	
    @org.springframework.beans.factory.annotation.Autowired
    public DatasetApiController(HttpServletRequest request) {
        this.request = request;
    }
	
	@Override
	public ResponseEntity<Void> deleteDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId)
			throws RestServiceException {
		if (datasetService.findById(datasetId) == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			datasetService.deleteById(datasetId);
		} catch (ShanoirException e) {
			if (DatasetsErrorModelCode.DATASET_NOT_FOUND.equals(e.getErrorCode())) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			} else if (e.getErrorMap() != null) {
				throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Forbidden",
						new ErrorDetails(e.getErrorMap())));
			}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<DatasetDTO> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId) {
		final Dataset dataset = datasetService.findById(datasetId);
		if (dataset == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		if (dataset instanceof MrDataset) {
			return new ResponseEntity<>(mrDatasetMapper.datasetToDatasetDTO((MrDataset) dataset), HttpStatus.OK);
		}
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(dataset), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> updateDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
			@ApiParam(value = "study to update", required = true) @Valid @RequestBody Dataset dataset,
			final BindingResult result) throws RestServiceException {
		// IMPORTANT : avoid any confusion that could lead to security breach
		dataset.setId(datasetId);

		// A basic dataset can only update certain fields, check that
		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(dataset);
		// Check hibernate validation
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		/* Update dataset in db. */
		try {
			datasetService.update(dataset);
		} catch (ShanoirException e) {
			LOG.error("Error while trying to update dataset " + datasetId + " : ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/*
	 * Get access rights errors.
	 *
	 * @param dataset dataset.
	 * 
	 * @return an error map.
	 */
	private FieldErrorMap getUpdateRightsErrors(final Dataset dataset) {
		final Dataset previousStateDataset = datasetService.findById(dataset.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<Dataset>().validate(previousStateDataset,
				dataset);
		return accessErrors;
	}

	/**
	 * @throws RestServiceException 
	 * 
	 */
	@Override
	public ResponseEntity<List<DatasetDTO>> findDatasets() throws RestServiceException {
		try {
			List<Dataset> datasets = datasetService.findAll();
			if (datasets.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
		} catch (ShanoirException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Cant get datasets", null));
		}
	}

	public ResponseEntity<ByteArrayResource> downloadDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii", defaultValue = "dcm") 
			@Valid @RequestParam(value = "format", required = false, defaultValue = "dcm") String format)
			throws RestServiceException, IOException {

		final Dataset dataset = datasetService.findById(datasetId);
		if (dataset == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Dataset with id not found.", null));
		}
		
		/*
		 * Create folder and file here:
		 */
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		long n = RANDOM.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}
		String tmpFilePath = tmpDir + File.separator + Long.toString(n);
		File workFolder = new File(tmpFilePath + DOWNLOAD);
		workFolder.mkdirs();
		File zipFile = new File(tmpFilePath + ZIP);
		zipFile.createNewFile();

		try {
			List<URL> pathURLs = new ArrayList<URL>();
			if ("dcm".equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
				downloader.downloadDicomFilesForURLs(pathURLs, workFolder);
			} else if ("nii".equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
				copyNiftiFilesForURLs(pathURLs, workFolder);
			} else {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
		} catch (IOException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error in WADORSDownloader.", null));
		} catch (MessagingException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error in WADORSDownloader.", null));
		}

		zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		return ResponseEntity.ok()
				// Content-Disposition
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
				// Content-Type
				.contentType(MediaType.parseMediaType(contentType)) //
				// Content-Length
				.contentLength(data.length) //
				.body(resource);
	}
	
	/**
	 * This method receives a list of URLs containing file:/// urls and copies the files to a folder named workFolder.
	 * @param urls
	 * @param workFolder
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void copyNiftiFilesForURLs(final List<URL> urls, final File workFolder) throws IOException {
		for (Iterator iterator = urls.iterator(); iterator.hasNext();) {
			URL url =  (URL) iterator.next();
			File srcFile = new File(url.getPath());
			File destFile = new File(workFolder.getAbsolutePath() + File.separator + srcFile.getName());
			Files.copy(srcFile.toPath(), destFile.toPath());
		}
	}

	/**
	 * This method reads all dataset files depending on the format attached to one dataset.
	 * @param dataset
	 * @param pathURLs
	 * @throws MalformedURLException
	 */
	private void getDatasetFilePathURLs(final Dataset dataset, List<URL> pathURLs, DatasetExpressionFormat format) throws MalformedURLException {
		List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
		for (Iterator itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
			DatasetExpression datasetExpression = (DatasetExpression) itExpressions.next();
			if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
				List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
				for (Iterator itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
					DatasetFile datasetFile = (DatasetFile) itFiles.next();
					URL url = new URL(datasetFile.getPath());
					pathURLs.add(url);
				}
			}
		}
	}
	
	private void zip(String sourceDirPath, String zipFilePath) throws IOException {
		Path p = Paths.get(zipFilePath);
		try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
			Path pp = Paths.get(sourceDirPath);
			Files.walk(pp)
				.filter(path -> !Files.isDirectory(path))
				.forEach(path -> {
					ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
					try {
						zs.putNextEntry(zipEntry);
						Files.copy(path, zs);
						zs.closeEntry();
					} catch (IOException e) {
						LOG.error(e.getMessage(), e);
					}
				});
            	zs.finish();
            zs.close();
		}
	}
    
}
