package org.shanoir.ng.dataset.controler;

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

import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	private DatasetService datasetService;

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

		try {
			datasetService.deleteById(datasetId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
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

		validate(dataset, result);

		try {
			datasetService.update(dataset);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<Page<DatasetDTO>> findDatasets(final Pageable pageable) throws RestServiceException {
		Page<Dataset> datasets = datasetService.findPage(pageable);	
		if (datasets.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Page<DatasetDTO>>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
	}

	@Override
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
		
		/* Create folder and file */
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
		} catch (IOException | MessagingException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error in WADORSDownloader.", null));
		} 
		zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
				.contentType(MediaType.parseMediaType(contentType))
				.contentLength(data.length)
				.body(resource);
	}
	
	/**
	 * Receives a list of URLs containing file:/// urls and copies the files to a folder named workFolder.
	 * @param urls
	 * @param workFolder
	 * @throws IOException
	 * @throws MessagingException
	 */
	private void copyNiftiFilesForURLs(final List<URL> urls, final File workFolder) throws IOException {
		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
			URL url =  (URL) iterator.next();
			File srcFile = new File(url.getPath());
			File destFile = new File(workFolder.getAbsolutePath() + File.separator + srcFile.getName());
			Files.copy(srcFile.toPath(), destFile.toPath());
		}
	}

	/**
	 * Reads all dataset files depending on the format attached to one dataset.
	 * @param dataset
	 * @param pathURLs
	 * @throws MalformedURLException
	 */
	private void getDatasetFilePathURLs(final Dataset dataset, List<URL> pathURLs, DatasetExpressionFormat format) throws MalformedURLException {
		List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
		for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
			DatasetExpression datasetExpression = (DatasetExpression) itExpressions.next();
			if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
				List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
				for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
					DatasetFile datasetFile = (DatasetFile) itFiles.next();
					URL url = new URL(datasetFile.getPath());
					pathURLs.add(url);
				}
			}
		}
	}
	
	/**
	 * Zip
	 * 
	 * @param sourceDirPath
	 * @param zipFilePath
	 * @throws IOException
	 */
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
	
	/**
	 * Validate a dataset
	 * 
	 * @param dataset
	 * @param result
	 * @throws RestServiceException
	 */
	private void validate(Dataset dataset, BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		} 
	}
}
