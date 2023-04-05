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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.dataset.dto.DatasetAndProcessingsDTOInterface;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetApiController implements DatasetApi {

	private static final String DCM = "dcm";

	private static final String ZIP = ".zip";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private static final String SUB_PREFIX = "sub-";

    private static final String SES_PREFIX = "ses-";

	private static final Logger LOG = LoggerFactory.getLogger(DatasetApiController.class);

	@Value("${datasets-data}")
	private String niftiStorageDir;

	@Autowired
	private DatasetMapper datasetMapper;

	@Autowired
	private MrDatasetMapper mrDatasetMapper;

	@Autowired
	private EegDatasetMapper eegDatasetMapper;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private ExaminationService examinationService;

	@Autowired
	private ImporterService importerService;

	@Autowired
	private WADODownloaderService downloader;

	@Autowired
	ShanoirEventService eventService;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	DatasetDownloaderServiceImpl datasetDownloaderService;

	/** Number of downloadable datasets. */
	private static final int DATASET_LIMIT = 50;

	@PostConstruct
	private void initialize() {
		// Set timeout to 1mn (consider nifti reconversion can take some time)
		this.rabbitTemplate.setReplyTimeout(60000);
	}

	@Override
	public ResponseEntity<Void> deleteDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId)
					throws RestServiceException {
		try {
			datasetService.deleteById(datasetId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			LOG.error("Error while deleting dataset. Please check DICOM server configuration.", e);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@Override
	public ResponseEntity<Void> deleteDatasets(
			@ApiParam(value = "ids of the datasets", required=true) @Valid
			@RequestBody(required = true) List<Long> datasetIds)
					throws RestServiceException {
		try {
			datasetService.deleteByIdIn(datasetIds);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<DatasetAndProcessingsDTOInterface> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId) {

		final Dataset dataset = datasetService.findById(datasetId);

		if (dataset == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (dataset instanceof MrDataset) {
			return new ResponseEntity<>(mrDatasetMapper.datasetToDatasetAndProcessingsDTO((MrDataset) dataset), HttpStatus.OK);
		}
		else if (dataset instanceof EegDataset) {
			return new ResponseEntity<>(eegDatasetMapper.datasetToDatasetAndProcessingsDTO((EegDataset) dataset), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(datasetMapper.datasetToDatasetAndProcessingsDTO(dataset), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Void> updateDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId,
			@ApiParam(value = "study to update", required = true) @Valid @RequestBody final Dataset dataset,
			final BindingResult result) throws RestServiceException {

		validate(result);

		try {
			datasetService.update(dataset);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);

		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<Page<DatasetDTO>> findDatasets(final Pageable pageable) throws RestServiceException {
		Page<Dataset> datasets = datasetService.findPage(pageable);
		if (datasets.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<DatasetDTO>> findDatasetsByIds(
			@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds) {
		List<Dataset> datasets = datasetService.findByIdIn(datasetIds);
		if (datasets.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
	}

    @Override
    public ResponseEntity<Long> getSizeByStudyId(Long studyId) {
		Long size = datasetService.getSizeByStudyId(studyId);

		return new ResponseEntity<>(Objects.requireNonNullElse(size, 0L), HttpStatus.OK);

	}

    @Override
	public ResponseEntity<List<DatasetDTO>> findDatasetsByAcquisitionId(@ApiParam(value = "id of the subject", required = true) @PathVariable("acquisitionId") Long acquisitionId) {
		List<Dataset> datasets = datasetService.findByAcquisition(acquisitionId);
		if (datasets.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
		}
	}
	
	@Override
	public ResponseEntity<List<DatasetDTO>> findDatasetsByStudycardId(@ApiParam(value = "id of the studycard", required = true) @PathVariable("studycardId") Long studycardId) {
		List<Dataset> datasets = datasetService.findByStudycard(studycardId);
		if (datasets.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<List<DatasetDTO>> findDatasetByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		
		final List<Examination> examinations = examinationService.findByStudyId(studyId);
		if (examinations.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		List<Dataset> datasets = new ArrayList<Dataset>();
		for(Examination examination : examinations) {
			List<DatasetAcquisition> datasetAcquisitions = examination.getDatasetAcquisitions();
			for(DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
				for(Dataset dataset : datasetAcquisition.getDatasets()) {
					datasets.add(dataset);
				}
			}
		}
		return new ResponseEntity<List<DatasetDTO>>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Long>> findDatasetIdsBySubjectIdStudyId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		List<Dataset> datasets = getBySubjectStudy(subjectId, studyId);
		return new ResponseEntity<>(datasets.stream().map(Dataset::getId).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<List<DatasetDTO>> findDatasetsBySubjectIdStudyId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		List<Dataset> datasets = getBySubjectStudy(subjectId, studyId);
		return new ResponseEntity<List<DatasetDTO>>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
	}

	private List<Dataset> getBySubjectStudy(Long subjectId, Long studyId) {
		final List<Examination> examinations = examinationService.findBySubjectIdStudyId(subjectId, studyId);

		List<Dataset> datasets = new ArrayList<>();
		for(Examination examination : examinations) {
			List<DatasetAcquisition> datasetAcquisitions = examination.getDatasetAcquisitions();
			for(DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
				for(Dataset dataset : datasetAcquisition.getDatasets()) {
					datasets.add(dataset);
				}
			}
		}
		return datasets;
	}
	
	@Override
	public void downloadDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId,
			@ApiParam(value = "Dowloading nifti, decide the nifti converter id") final Long converterId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = DCM)
			@Valid @RequestParam(value = "format", required = false, defaultValue = DCM) final String format, HttpServletResponse response)
					throws RestServiceException, IOException {
		this.datasetDownloaderService.downloadDatasetById(datasetId, converterId, format, response);
	}

	@Override
	public ResponseEntity<String> getDicomMetadataByDatasetId(
    		@ApiParam(value = "id of the dataset", required=true) @PathVariable("datasetId") Long datasetId) throws IOException, MessagingException {	
		final Dataset dataset = datasetService.findById(datasetId);		
		List<URL> pathURLs = new ArrayList<>();
		DatasetFileUtils.getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
		if (pathURLs.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(downloader.downloadDicomMetadataForURL(pathURLs.get(0)), HttpStatus.OK);			
		}
	}
	
	public ResponseEntity<Void> createProcessedDataset(@ApiParam(value = "ProcessedDataset to create" ,required=true )  @Valid @RequestBody ProcessedDatasetImportJob importJob) throws IOException {
		importerService.createProcessedDataset(importJob);
		File originalNiftiName = new File(importJob.getProcessedDatasetFilePath());
		importerService.cleanTempFiles(originalNiftiName.getParent());
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@Override
	public void massiveDownloadByDatasetIds(
			@ApiParam(value = "ids of the datasets", required=true) @Valid
			@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg, BIDS", defaultValue = DCM) @Valid
			@RequestParam(value = "format", required = false, defaultValue=DCM) String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, MalformedURLException, IOException {
		// STEP 0: Check data integrity
		if (datasetIds == null || datasetIds.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.FORBIDDEN.value(), "Please use a valid sets of dataset IDs."));
		}

		if (datasetIds.size() > DATASET_LIMIT) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.FORBIDDEN.value(), "You can't download more than " + DATASET_LIMIT + " datasets."));
		}

		// STEP 1: Retrieve all datasets all in one with only the one we can see
		List<Dataset> datasets = datasetService.findByIdIn(datasetIds);

		datasetDownloaderService.massiveDownload(format, datasets, response, false);
	}	

	@Override
	public void massiveDownloadByStudyId(
			@ApiParam(value = "id of the study", required=true) @Valid
			@RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg, BIDS", defaultValue = DCM) @Valid
			@RequestParam(value = "format", required = false, defaultValue=DCM) String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, IOException {
		// STEP 0: Check data integrity
		if (studyId == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.FORBIDDEN.value(), "Please use a valid study ID."));
		}
		// STEP 1: Retrieve all datasets all in one with only the one we can see
		List<Dataset> datasets = datasetService.findByStudyId(studyId);

		if (datasets.size() > DATASET_LIMIT) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.FORBIDDEN.value(), "This study has more than " + DATASET_LIMIT + " datasets, that is the limit. Please download them from solr search." ));
		}

		datasetDownloaderService.massiveDownload(format, datasets, response, false);
	}

	@Override
	public void massiveDownloadByExaminationId(
			@ApiParam(value = "id of the examination", required=true) @Valid
			@RequestParam(value = "examinationId", required = true) Long examinationId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg, BIDS", defaultValue = DCM) @Valid
			@RequestParam(value = "format", required = false, defaultValue=DCM) String format, HttpServletResponse response) throws RestServiceException, EntityNotFoundException, IOException {
		// STEP 0: Check data integrity
		if (examinationId == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.FORBIDDEN.value(), "Please use a valid examination ID."));
		}
		// STEP 1: Retrieve all datasets all in one
		List<Dataset> datasets = datasetService.findByExaminationId(examinationId);

		if (datasets.size() > DATASET_LIMIT) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.FORBIDDEN.value(), "You can't download more than " + DATASET_LIMIT + " datasets."));
		}

		datasetDownloaderService.massiveDownload(format, datasets, response, true);
	}

	/**
	 * Zip a single file
	 * 
	 * @param sourceFile
	 * @param zipFile
	 * @throws IOException
	 */
	private void zipSingleFile(final File sourceFile, final File zipFile) throws IOException {

		byte[] buffer = new byte[1024];


		try (	FileOutputStream fos = new FileOutputStream(zipFile);
				ZipOutputStream zos = new ZipOutputStream(fos);
				FileInputStream fis = new FileInputStream(sourceFile);
				) {
			// begin writing a new ZIP entry, positions the stream to the start of the entry data
			zos.putNextEntry(new ZipEntry(sourceFile.getName()));

			int length;

			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
		}
	}

	private File recreateFile(final String fileName) throws IOException {
		File file = new File(fileName);
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		return file;
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
			URL url =  iterator.next();
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
	 * @param result
	 * @throws RestServiceException
	 */
	private void validate(final BindingResult result) throws RestServiceException {
		final FieldErrorMap errors = new FieldErrorMap(result);
		if (!errors.isEmpty()) {
			ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors));
			throw new RestServiceException(error);
		}
	}

	/**
	 * This enum is for coordinates system and associated units
	 */
	public enum CoordinatesSystem {
		ACPC("mm"),
		ALLEN("mm"),
		ANALYZE("mm"),
		BTI_4D("m"),
		CTF_MRI("mm"),
		CTF_GRADIOMETER("cm"),
		CAPTRAK("mm"),
		CHIETI("mm"),
		DICOM("mm"),
		FREESURFER("mm"),
		MNI("mm"),
		NIFTI("mm"),
		NEUROMAG_ELEKTA("m"),
		PAXINOS_FRANKLIN("mm"),
		TALAIRACH_TOURNOUX("mm"),
		YOKOGAWA("n/a");

		private String unit;

		CoordinatesSystem(final String pUnit) {
			this.unit = pUnit;
		}
		public String getUnit() {
			return unit;
		}
	}

	@Override
	public ResponseEntity<ByteArrayResource> downloadStatistics(
			@ApiParam(value = "Study name including regular expression", required=false) @Valid
			@RequestParam(value = "studyNameInRegExp", required = false) String studyNameInRegExp,
			@ApiParam(value = "Study name excluding regular expression", required=false) @Valid
			@RequestParam(value = "studyNameOutRegExp", required = false) String studyNameOutRegExp,
			@ApiParam(value = "Subject name including regular expression", required=false) @Valid
			@RequestParam(value = "subjectNameInRegExp", required = false) String subjectNameInRegExp,
			@ApiParam(value = "Subject name excluding regular expression", required=false) @Valid
			@RequestParam(value = "subjectNameOutRegExp", required = false) String subjectNameOutRegExp
			) throws RestServiceException, IOException {
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
		File statisticsFile = recreateFile(userDir + File.separator + "shanoirExportStatistics.txt");
		File zipFile = recreateFile(userDir + File.separator + "shanoirExportStatistics" + ZIP);

		// Get the data
		try (	FileOutputStream fos = new FileOutputStream(statisticsFile);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));	){


			List<Object[]> results = datasetService.queryStatistics(studyNameInRegExp, studyNameOutRegExp, subjectNameInRegExp, subjectNameOutRegExp);

			for (Object[] or : results) {
				List<String> strings = Arrays.stream(or).map(object -> Objects.toString(object, null)).collect(Collectors.toList());
				bw.write(String.join("\t", strings));
				bw.newLine();
			}

		} catch (javax.persistence.NoResultException e) {
			throw new RestServiceException(new ErrorModel(HttpStatus.NOT_FOUND.value(), "No result found.", e));
		} catch (Exception e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while querying the database.", e));
		}

		zipSingleFile(statisticsFile, zipFile);

		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		statisticsFile.delete();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.contentLength(data.length)
				.body(resource);
	}
}
