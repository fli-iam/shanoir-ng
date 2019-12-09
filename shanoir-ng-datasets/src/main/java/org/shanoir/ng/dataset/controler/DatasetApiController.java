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

import org.shanoir.ng.dataset.DatasetDescription;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.DatasetUrlsDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDatasetNature;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonMappingException;


import io.swagger.annotations.ApiParam;

@Controller
public class DatasetApiController implements DatasetApi {

	private static final String ZIP = ".zip";

	private static final String DOWNLOAD = ".download";
	
	private static final String BOUTIQUES = ".boutiques";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	
	private static final String T1w = "T1w";
	
	private static final String SUB_PREFIX = "sub-";
	
	private static final String SES_PREFIX = "ses-";
	
	private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";
	
	private static final String README_FILE = "README";
	
	private static final Logger LOG = LoggerFactory.getLogger(DatasetApiController.class);
	
	@Value("${datasets-data}")
	private String niftiStorageDir;

	@Autowired
	private DatasetMapper datasetMapper;

	@Autowired
	private MrDatasetMapper mrDatasetMapper;
	
	@Autowired
	private DatasetService datasetService;
	
	@Autowired 
	private ExaminationService examinationService;

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

	@Override
	public ResponseEntity<DatasetUrlsDTO> getDatasetUrlsById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") Long datasetId)
			throws RestServiceException, IOException {

		final Dataset dataset = datasetService.findById(datasetId);
		if (dataset == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Dataset with id not found.", null));
		}
		
		DatasetUrlsDTO urls = new DatasetUrlsDTO();

		try {
			List<URL> pathURLs = new ArrayList<URL>();
			getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
			for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
				URL url = iterator.next();
				String urlString = url.toString();
				System.out.println(urlString);
				urls.dcm.add(urlString);
			}
			pathURLs.clear();
			getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
			for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
				URL url = iterator.next();
				urls.nii.add(url.toString());
				String urlString = url.toString();
				System.out.println(urlString);
				urls.nii.add(urlString);
			}
			
		} catch (IOException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error in WADORSDownloader.", null));
		}
		return new ResponseEntity<DatasetUrlsDTO>(urls, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> prepareDatasetUrl(
			@ApiParam(value = "study to update", required = true) @Valid @RequestBody ObjectNode urlObject,
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
		File workFolder = new File(tmpFilePath + BOUTIQUES);
		workFolder.mkdirs();
		String url = urlObject.get("url").asText();
		try {
			List<URL> pathURLs = new ArrayList<URL>();

			if ("dcm".equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
				downloader.downloadDicomFilesForURLs(pathURLs, workFolder);
			} else if ("nii".equals(format)) {
				pathURLs.add(new URL(url));
				copyNiftiFilesForURLs(pathURLs, workFolder);
			} else {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
			
		} catch (IOException | MessagingException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error in WADORSDownloader.", null));
		}
		return new ResponseEntity<String>(workFolder.getPath(), HttpStatus.OK);
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
					URL url = new URL(datasetFile.getPath().replaceAll("%20", " "));
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
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(p))) {
			Path pp = Paths.get(sourceDirPath);
			Files.walk(pp)
				.filter(path -> !Files.isDirectory(path))
				.forEach(path -> {
					ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
					try {
						zos.putNextEntry(zipEntry);
						Files.copy(path, zos);
						zos.closeEntry();
					} catch (IOException e) {
						LOG.error(e.getMessage(), e);
					}
				});
            	zos.finish();
            zos.close();
		}
	}
	
	@Override
	public ResponseEntity<ByteArrayResource> exportBIDSBySubjectId(@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "name of the subject", required = true) @PathVariable("subjectName") String subjectName,
			@ApiParam(value = "name of the study", required = true) @PathVariable("studyName") String studyName)
			throws RestServiceException, MalformedURLException, IOException {
		final List<Examination> examinationList = examinationService.findBySubjectId(subjectId);
		if (examinationList.isEmpty()) { 
			throw new RestServiceException(
				new ErrorModel(HttpStatus.NOT_FOUND.value(), "No Examination found of subject Id.", null)); 
		} else {
			// 1. Create folder
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
			
			// 2. Create dataset_description.json and README
			DatasetDescription datasetDesciption = new DatasetDescription();
			datasetDesciption.setName(studyName);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + DATASET_DESCRIPTION_FILE), datasetDesciption);
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + README_FILE), studyName);
			
			// TODO BIDS: 3. Create [ses-<label>/] folder if multi exams 
			/*if (examinationList.size() > 1) {
				for (Examination examination: examinationList) {
					String sesLabel = examination.getId().toString();
					final List<DatasetAcquisition> datasetAcquisitionList = examination.getDatasetAcquisitions();
					for (DatasetAcquisition datasetAcquisition : datasetAcquisitionList) {
						// TODO BIDS: 5. multi dataset acquisiton: add [_acq-<label>]
						String acqLabel = datasetAcquisition.getId().toString();
						final List<Dataset> datasetList = datasetAcquisition.getDatasets();
						for (Dataset dataset: datasetList) {
							// TODO BIDS: 6. multi datasets: add [_run-<index>]
							
							// TODO BIDS: 7. multi MrDatasetNature: add _<modality_label>
						}
					}
				}
			}*/
			
			// 8. Get modality label, nii and json of dataset
			/* For the demo: one exam, one acq, one dataset, one modality which is T1 */
			final Dataset dataset = examinationList.get(0).getDatasetAcquisitions().get(0).getDatasets().get(0);
			if (dataset == null) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.NOT_FOUND.value(), "No Dataset found for subject Id.", null));
			}
			
			// Get modality label
			String modalityLabel = null;
			if (((MrDataset) dataset).getUpdatedMrMetadata().getMrDatasetNature().equals(MrDatasetNature.T1_WEIGHTED_MR_DATASET)
					|| ((MrDataset) dataset).getUpdatedMrMetadata().getMrDatasetNature().equals(MrDatasetNature.T1_WEIGHTED_DCE_MR_DATASET)) {
				modalityLabel = T1w;
			} 
			if (StringUtils.isEmpty(modalityLabel)) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.NOT_FOUND.value(), "No MrDatasetNature, so could not define modality label and export BIDS!", null));
			}
			
			// Get nii and json files
			try {
				List<URL> pathURLs = new ArrayList<URL>();
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
				copyFilesForBIDSExport(pathURLs, workFolder, subjectName, examinationList.get(0).getId().toString(), modalityLabel);
			} catch (IOException e) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error exporting nifti files for subject in BIDS.", null));
			}
			
			// 9. Create zip file
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
	}
    
	/**
	 * This method receives a list of URLs containing file:/// urls and copies the files to a folder named workFolder.
	 * @param urls
	 * @param workFolder
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void copyFilesForBIDSExport(final List<URL> urls, final File workFolder, final String subjectName, 
			final String sesId, final String modalityLabel) throws IOException {
		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
			URL url =  (URL) iterator.next();
			File srcFile = new File(url.getPath());
			String destFilePath = srcFile.getPath().substring(niftiStorageDir.length() + 1, srcFile.getPath().lastIndexOf('/'));
			File destFolder = new File(workFolder.getAbsolutePath() + File.separator + destFilePath);
			destFolder.mkdirs();
			String extensionType = srcFile.getPath().substring(srcFile.getPath().lastIndexOf(".") + 1);
			String destFileNameBIDS = SUB_PREFIX + subjectName + "_" + SES_PREFIX + sesId + "_" + modalityLabel + "." + extensionType;
			File destFile = new File(destFolder.getAbsolutePath() + File.separator + destFileNameBIDS);
			Files.copy(srcFile.toPath(), destFile.toPath());
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
