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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.dataset.dto.DatasetAndProcessingsDTOInterface;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.service.DicomServiceApi;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetApiController implements DatasetApi {

	private static final String EEG = "eeg";

	private static final String NII = "nii";

	private static final String DCM = "dcm";

	private static final String ZIP = ".zip";

	private static final String DOWNLOAD = ".download";

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

	private final HttpServletRequest request;

	@Autowired
	private WADODownloaderService downloader;

	@Autowired
	private BIDSService bidsService;

	@Autowired
	private DatasetSecurityService datasetSecurityService;

	@Autowired
	private SubjectRepository subjectRepo;

	@Autowired
	private StudyRepository studyRepo;

	@Autowired
	ShanoirEventService eventService;
	
	@Autowired
	@Qualifier("stowrs")
	DicomServiceApi stowRsService;

	@Autowired
	@Qualifier("cstore")
	DicomServiceApi cStoreService;
	
	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;
	
	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePortWeb;

	@Value("${dcm4chee-arc.dicom.web}")
	private boolean dicomWeb;

	/** Number of downloadable datasets. */
	private static final int DATASET_LIMIT = 50;

	@org.springframework.beans.factory.annotation.Autowired
	public DatasetApiController(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public ResponseEntity<Void> deleteDataset(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId)
					throws RestServiceException {

		try {
			Dataset dataset = datasetService.findById(datasetId);
			bidsService.deleteDataset(dataset);
			datasetService.deleteById(datasetId);
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
	public ResponseEntity<List<Long>> findDatasetIdsBySubjectId(@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId) {
		final List<Examination> examinations = examinationService.findBySubjectId(subjectId);

		List<Long> datasetIds = new ArrayList<Long>();
		for(Examination examination : examinations) {
			ResponseEntity<List<Long>> response = findDatasetIdsBySubjectIdStudyId(subjectId, examination.getStudyId());
			if(response.getStatusCode() == HttpStatus.OK) {
				datasetIds.addAll(response.getBody());
			}
		}
		return new ResponseEntity<List<Long>>(datasetIds, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<DatasetDTO>> findDatasetsByAcquisitionId(@ApiParam(value = "id of the subject", required = true) @PathVariable("acquisitionId") Long acquisitionId) {

		List<Dataset> datasets = datasetService.findByAcquisition(acquisitionId);
		return new ResponseEntity<List<DatasetDTO>>(datasetMapper.datasetToDatasetDTO(datasets), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<Long>> findDatasetIdsBySubjectIdStudyId(
			@ApiParam(value = "id of the subject", required = true) @PathVariable("subjectId") Long subjectId,
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {

		final List<Examination> examinations = examinationService.findBySubjectIdStudyId(subjectId, studyId);
		if (examinations.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		List<Long> datasetIds = new ArrayList<Long>();
		for(Examination examination : examinations) {
			List<DatasetAcquisition> datasetAcquisitions = examination.getDatasetAcquisitions();
			for(DatasetAcquisition datasetAcquisition : datasetAcquisitions) {
				for(Dataset dataset : datasetAcquisition.getDatasets()) {
					datasetIds.add(dataset.getId());
				}
			}
		}
		return new ResponseEntity<>(datasetIds, HttpStatus.OK);
	}

	@Override
	public void downloadDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = DCM)
			@Valid @RequestParam(value = "format", required = false, defaultValue = DCM) final String format, HttpServletResponse response)
					throws RestServiceException, IOException {

		final Dataset dataset = datasetService.findById(datasetId);
		if (dataset == null) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.NOT_FOUND.value(), "Dataset with id not found.", null));
		}

		/* Create folder and file */
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		File userDir = getUserImportDir(tmpDir);

		String datasetName = "";
		datasetName += dataset.getId() + "-" + dataset.getName();
		if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
			datasetName += "-" + dataset.getUpdatedMetadata().getComment();
		}

		String tmpFilePath = userDir + File.separator + datasetName + "_" + format;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		File workFolder = new File(tmpFilePath + "-" + formatter.format(new DateTime().toDate()) + DOWNLOAD);
		workFolder.mkdirs();

		try {
			List<URL> pathURLs = new ArrayList<>();
			String subjectName = subjectRepo.findOne(dataset.getSubjectId()).getName();
			if (subjectName == null) {
				subjectName = "unknown";
			}

			if (DCM.equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
				downloader.downloadDicomFilesForURLs(pathURLs, workFolder, subjectName);
			} else if (NII.equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
				copyNiftiFilesForURLs(pathURLs, workFolder, dataset, subjectName);
			} else if (EEG.equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
				copyNiftiFilesForURLs(pathURLs, workFolder, dataset, subjectName);
			} else {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
		} catch (IOException | MessagingException e) {
			LOG.error("Error while retrieveing dataset data.", e);
			FileUtils.deleteQuietly(workFolder);

			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while retrieveing dataset data.", e));
		}
		File zipFile = new File(tmpFilePath + ZIP);
		zipFile.createNewFile();

		zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, dataset.getId().toString(), KeycloakUtil.getTokenUserId(), dataset.getId().toString() + "." + format, ShanoirEvent.IN_PROGRESS);
		eventService.publishEvent(event);

		try (InputStream is = new FileInputStream(zipFile);) {
			response.setHeader("Content-Disposition", "attachment;filename=" + zipFile.getName());
			response.setContentType(contentType);
			response.setContentLengthLong(zipFile.length());
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
			event.setStatus(ShanoirEvent.SUCCESS);
			eventService.publishEvent(event);
		} finally {
			FileUtils.deleteQuietly(workFolder);
			FileUtils.deleteQuietly(zipFile);
		}
	}

	@Override
	public void massiveDownloadByDatasetIds(
			@ApiParam(value = "ids of the datasets", required=true) @Valid
			@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = DCM) @Valid
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

		massiveDownload(format, datasets, response);
	}

	@Override
	public void massiveDownloadByStudyId(
			@ApiParam(value = "id of the study", required=true) @Valid
			@RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = DCM) @Valid
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

		massiveDownload(format, datasets, response);
	}

	public void massiveDownload(String format, List<Dataset> datasets, HttpServletResponse response) throws EntityNotFoundException, RestServiceException, IOException {
		// STEP 2: Check rights => Also filters datasets on rights
		datasets = datasetSecurityService.hasRightOnAtLeastOneDataset(datasets, "CAN_DOWNLOAD");
		// STEP 3: Get the data
		// Check rights on at least one of the datasets and filter the datasetIds list
		File userDir = getUserImportDir(System.getProperty(JAVA_IO_TMPDIR));

		// Add timestamp to get a difference
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		File tmpFile = new File(userDir.getAbsolutePath() + File.separator + "Datasets" + formatter.format(new DateTime().toDate()));
		tmpFile.mkdirs();

		// Get the data
		try {
			for (Dataset dataset : datasets) {
				// Create a new folder organized by subject / examination
				String subjectName = subjectRepo.findOne(dataset.getSubjectId()).getName();
				String studyName = studyRepo.findOne(dataset.getStudyId()).getName();

				Examination exam = dataset.getDatasetAcquisition().getExamination();
				String datasetFilePath = studyName + "_" + subjectName + "_Exam-" + exam.getId() + "-" + exam.getComment();
				datasetFilePath = datasetFilePath. replaceAll("[^a-zA-Z0-9_\\-]", "_");
				if(datasetFilePath.length() > 255 ){
					datasetFilePath = datasetFilePath.substring(0, 254);
				}
				datasetFilePath = tmpFile.getAbsolutePath() + File.separator + datasetFilePath;
				File datasetFile = new File(datasetFilePath);
				if (!datasetFile.exists()) {
					datasetFile.mkdir();
				}

				List<URL> pathURLs = new ArrayList<>();

				if (dataset instanceof EegDataset) {
					getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
					copyNiftiFilesForURLs(pathURLs, datasetFile, dataset, subjectName);
				} else if (DCM.equals(format)) {
					getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
					downloader.downloadDicomFilesForURLs(pathURLs, datasetFile, subjectName);
				} else if (NII.equals(format)) {
					getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
					copyNiftiFilesForURLs(pathURLs, datasetFile, dataset, subjectName);
				} else {
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Please choose either nifti, dicom or eeg file type.", null));
				}
			}
		} catch (IOException | MessagingException e) {
			LOG.error("Error while copying files: ", e);
			FileUtils.deleteQuietly(tmpFile);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while retrieving files. Please contact an administrator.", e));
		} catch(OutOfMemoryError error) {
			LOG.error("Out of memory error while copying files: ", error);
			FileUtils.deleteQuietly(tmpFile);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "The size of data you tried to download is too Important. Please split your download.", error));
		}
		// Zip it
		File zipFile = new File(tmpFile.getAbsolutePath() + ZIP);
		zipFile.createNewFile();
		zip(tmpFile.getAbsolutePath(), zipFile.getAbsolutePath());

		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

		String ids = String.join(",", datasets.stream().map(dataset -> dataset.getId().toString()).collect(Collectors.toList()));
		ShanoirEvent event = new ShanoirEvent(ShanoirEventType.DOWNLOAD_DATASET_EVENT, ids, KeycloakUtil.getTokenUserId(), ids + "." + format, ShanoirEvent.IN_PROGRESS);
		eventService.publishEvent(event);

		try (InputStream is = new FileInputStream(zipFile);) {
			response.setHeader("Content-Disposition", "attachment;filename=" + zipFile.getName());
			response.setContentType(contentType);
			response.setContentLengthLong(zipFile.length());
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
			event.setStatus(ShanoirEvent.SUCCESS);
			eventService.publishEvent(event);
		} finally {
			FileUtils.deleteQuietly(tmpFile);
			FileUtils.deleteQuietly(zipFile);
		}
	}

	/**
	 * Receives a list of URLs containing file:/// urls and copies the files to a folder named workFolder.
	 * @param urls
	 * @param workFolder
	 * @param subjectName the subjectName
	 * @throws IOException
	 * @throws MessagingException
	 */
	private void copyNiftiFilesForURLs(final List<URL> urls, final File workFolder, Dataset dataset, Object subjectName) throws IOException {
		int index = 0;
		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
			URL url =  iterator.next();
			File srcFile = new File(UriUtils.decode(url.getPath(), "UTF-8"));

			// Theorical file name:  NomSujet_SeriesDescription_SeriesNumberInProtocol_SeriesNumberInSequence.nii
			StringBuilder name = new StringBuilder("");

			name.append(subjectName).append("_")
			.append(dataset.getUpdatedMetadata().getComment()).append("_")
			.append(dataset.getDatasetAcquisition().getSortingIndex()).append("_");
			if (dataset.getUpdatedMetadata().getName() != null && dataset.getUpdatedMetadata().getName().lastIndexOf(" ") != -1) {
				name.append(dataset.getUpdatedMetadata().getName().substring(dataset.getUpdatedMetadata().getName().lastIndexOf(" ") + 1)).append("_");
			}
			name.append(dataset.getDatasetAcquisition().getRank()).append("_")
			.append(index)
			.append(".").append(FilenameUtils.getExtension(srcFile.getName()));

			File destFile = new File(workFolder.getAbsolutePath() + File.separator + name);
			Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			index++;
		}
	}

	/**
	 * Reads all dataset files depending on the format attached to one dataset.
	 * @param dataset
	 * @param pathURLs
	 * @throws MalformedURLException
	 */
	private void getDatasetFilePathURLs(final Dataset dataset, final List<URL> pathURLs, final DatasetExpressionFormat format) throws MalformedURLException {
		List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
		for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
			DatasetExpression datasetExpression = itExpressions.next();
			if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
				List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
				for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
					DatasetFile datasetFile = itFiles.next();
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
	private void zip(final String sourceDirPath, final String zipFilePath) throws IOException {
		Path p = Paths.get(zipFilePath);
		// 1. Create an outputstream (zip) on the destination
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(p))) {

			// 2. "Walk" => iterate over the source file
			Path pp = Paths.get(sourceDirPath);
			try(Stream<Path> walker = Files.walk(pp)) {

				// 3. We only consider directories, and we copyt them directly by "relativising" them then copying them to the output
				walker.filter(path -> !path.toFile().isDirectory())
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
			}
			zos.finish();
		}
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

	public static File getUserImportDir(String importDir) {
		final Long userId = KeycloakUtil.getTokenUserId();
		final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
		final File userImportDir = new File(userImportDirFilePath);
		if (!userImportDir.exists()) {
			userImportDir.mkdirs(); // create if not yet existing
		} // else is wanted case, user has already its import directory
		return userImportDir;
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
		File userDir = getUserImportDir(tmpDir);
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

	@Override
	public ResponseEntity<Dataset> addDatasetFile (
			@ApiParam(value = "Id of the dataset", required = true) @PathVariable("datasetId") Long datasetId,
			@ApiParam(value = "Id of the datasetFile", required = true)  @PathVariable("datasetFileId") Long datasetFileId,
			@ApiParam(value = "Linked file", required = true) @RequestPart("file") MultipartFile multipartFile)
					throws RestServiceException {
		// Load dataset file
		try {
			LOG.error("Receiving dataset file" + multipartFile + " " + datasetFileId);
			Dataset dataset = datasetService.findById(datasetId);
			DatasetFile dsFile = null;
			for (DatasetExpression expression : dataset.getDatasetExpressions()) {
				for (DatasetFile dsFileIt : expression.getDatasetFiles()) {
					if (dsFileIt.getId().equals(datasetFileId)) {
						dsFile = dsFileIt;
						// Move the file to the adapted path
						if (dsFile.isPacs()) {
							// Move to PACS
							File destination = new File("/tmp/migration" + LocalDateTime.now() + File.separator + multipartFile.getName());
							destination.getParentFile().mkdirs();
							multipartFile.transferTo(destination);
							// Does this actually works ?
							if (dicomWeb) {
								stowRsService.sendDicomFilesToPacs(destination.getParentFile());
							} else {
								cStoreService.sendDicomFilesToPacs(destination.getParentFile());
							}
							FileUtils.deleteQuietly(destination.getParentFile());
						} else {
							// MOVE nifti (and others) on disc
							File destination = new File(dsFile.getPath().replace("file://", ""));
							destination.getParentFile().mkdirs();
							multipartFile.transferTo(destination);
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error while importing dataset file: ", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Dataset> createNewDatasets(
			@ApiParam(value = "Dataset to create", required=true) @RequestBody Dataset dataset,
			final BindingResult result) throws RestServiceException {
		// Principally used by migration API
		for (DatasetExpression expression : dataset.getDatasetExpressions()) {
			expression.setDataset(dataset);
			for (DatasetFile file : expression.getDatasetFiles()) {
				file.setDatasetExpression(expression);
				
				// Replace PACS reference
				if (file.isPacs()) {
					String oldPath = file.getPath();
					String newPath = oldPath.replaceAll("http(.*):[0-9]{4,6}", dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePortWeb);
					file.setPath(newPath);
				}
			}
		}
		Dataset created = datasetService.create(dataset);

		for (DatasetExpression expression : created.getDatasetExpressions()) {
			expression.setDataset(null);
			for (DatasetFile file : expression.getDatasetFiles()) {
				file.setDatasetExpression(null);
			}
		}
		return new ResponseEntity<>(created, HttpStatus.OK);
	}
}
