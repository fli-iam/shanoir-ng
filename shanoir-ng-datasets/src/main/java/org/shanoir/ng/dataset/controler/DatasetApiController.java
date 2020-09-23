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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetApiController implements DatasetApi {
	
	@PersistenceContext
	private EntityManager em;

	private static final String EEG = "eeg";

	private static final String NII = "nii";

	private static final String DCM = "dcm";

	private static final String ATTACHMENT_FILENAME = "attachment;filename=";

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

	private static final SecureRandom RANDOM = new SecureRandom();

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
	public ResponseEntity<DatasetDTO> findDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId) {

		final Dataset dataset = datasetService.findById(datasetId);
		if (dataset == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		if (dataset instanceof MrDataset) {
			return new ResponseEntity<>(mrDatasetMapper.datasetToDatasetDTO((MrDataset) dataset), HttpStatus.OK);
		}
		else if (dataset instanceof EegDataset) {
			return new ResponseEntity<>(eegDatasetMapper.datasetToDatasetDTO((EegDataset) dataset), HttpStatus.OK);
		}
		return new ResponseEntity<>(datasetMapper.datasetToDatasetDTO(dataset), HttpStatus.OK);
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
	public ResponseEntity<ByteArrayResource> downloadDatasetById(
			@ApiParam(value = "id of the dataset", required = true) @PathVariable("datasetId") final Long datasetId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = DCM)
			@Valid @RequestParam(value = "format", required = false, defaultValue = DCM) final String format)
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
			List<URL> pathURLs = new ArrayList<>();
			if (DCM.equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
				downloader.downloadDicomFilesForURLs(pathURLs, workFolder);
			} else if (NII.equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
				copyNiftiFilesForURLs(pathURLs, workFolder, dataset);
			} else if (EEG.equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
				copyNiftiFilesForURLs(pathURLs, workFolder, dataset);
			} else {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
			}
		} catch (IOException | MessagingException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error in WADORSDownloader.", e.getLocalizedMessage()));
		}
		zip(workFolder.getAbsolutePath(), zipFile.getAbsolutePath());

		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME + zipFile.getName())
				.contentType(MediaType.parseMediaType(contentType))
				.contentLength(data.length)
				.body(resource);
	}

	@Override
	public ResponseEntity<ByteArrayResource> massiveDownloadByDatasetIds(
			@ApiParam(value = "ids of the datasets", required=true) @Valid
			@RequestParam(value = "datasetIds", required = true) List<Long> datasetIds,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = DCM) @Valid
			@RequestParam(value = "format", required = false, defaultValue=DCM) String format) throws RestServiceException, EntityNotFoundException, MalformedURLException, IOException {
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

		return massiveDownload(format, datasets);
	}

	@Override
	public ResponseEntity<ByteArrayResource> massiveDownloadByStudyId(
			@ApiParam(value = "id of the study", required=true) @Valid
			@RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = DCM) @Valid
			@RequestParam(value = "format", required = false, defaultValue=DCM) String format) throws RestServiceException, EntityNotFoundException, IOException {
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

		return massiveDownload(format, datasets);
	}

	public ResponseEntity<ByteArrayResource> massiveDownload(String format, List<Dataset> datasets) throws EntityNotFoundException, RestServiceException, IOException {
		// STEP 2: Check rights => Also filters datasets on rights
		datasets = datasetSecurityService.hasRightOnAtLeastOneDataset(datasets, "CAN_DOWNLOAD");
		// STEP 3: Get the data
		// Check rights on at least one of the datasets and filter the datasetIds list
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		String tmpFilePath = tmpDir + File.separator + "Datasets";

		File tmpFile = new File(tmpFilePath);
		tmpFile.mkdirs();

		// Get the data
		try {
			for (Dataset dataset : datasets) {
				// Create a new folder for every dataset
				File datasetFile = new File(tmpFile.getAbsolutePath() + File.separator + dataset.getId());
				datasetFile.mkdir();
 
				List<URL> pathURLs = new ArrayList<>();
				if (DCM.equals(format)) {
					getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.DICOM);
					downloader.downloadDicomFilesForURLs(pathURLs, datasetFile);
				} else if (NII.equals(format)) {
					getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
					copyNiftiFilesForURLs(pathURLs, datasetFile, dataset);
				}  else if (EEG.equals(format)) {
					getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
					copyNiftiFilesForURLs(pathURLs, datasetFile, dataset);
				} else {
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Please choose either nifti, dicom or eeg file type.", null));
				}
			}
		} catch (IOException | MessagingException e) {
			LOG.error("Error while copying files: ", e);
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while retrieving files. Please contact an administrator.", e));
		}
		// Zip it
		File zipFile = new File(tmpFilePath + ZIP);
		zipFile.createNewFile();

		zip(tmpFile.getAbsolutePath(), zipFile.getAbsolutePath());

		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		FileUtils.deleteDirectory(tmpFile);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
				.contentType(MediaType.MULTIPART_FORM_DATA)
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
	private void copyNiftiFilesForURLs(final List<URL> urls, final File workFolder, Dataset dataset) throws IOException {
		for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
			URL url =  iterator.next();
			File srcFile = new File(url.getPath());

			// Theorical file name:  NomSujet_SeriesDescription_SeriesNumberInProtocol_SeriesNumberInSequence.nii
			StringBuilder name = new StringBuilder("");
			
			Subject subject = subjectRepo.findOne(dataset.getSubjectId());
			if (subject != null) {
				name.append(subject.getName());
			} else {
				name.append("unknown");
			}
			name.append("_")
			.append(dataset.getUpdatedMetadata().getComment()).append("_")
			.append(dataset.getDatasetAcquisition().getSortingIndex()).append("_")
			.append(dataset.getDatasetAcquisition().getRank()).append(".")
			.append(FilenameUtils.getExtension(srcFile.getName()));

			File destFile = new File(workFolder.getAbsolutePath() + File.separator + name);
			Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
	public ResponseEntity<ByteArrayResource> downloadStatistics() throws RestServiceException, IOException {
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		String tmpFilePath = tmpDir + File.separator + "statistics.zip";
		File tmpFile = new File(tmpFilePath);
		tmpFile.mkdirs();

		// Get the data
		try {

			// var processBuilder = new ProcessBuilder();
			// processBuilder.command("notepad.exe");
			// var process = processBuilder.start();
			// var ret = process.waitFor();
			// System.out.printf("Program exited with code: %d", ret);
			
			Query mrQuery = em.createNativeQuery("select * from studies.study;");

			// 	"""
			// select 
			// sb.id as patient_id, 
			// sb.name as shanoir_name, 
			// sb.identifier as double_hash, 
			// -- pseud.birth_name_hash1 as birthname1,
			// -- pseud.birth_name_hash2 as birthname2,
			// -- pseud.birth_name_hash3 as birthname3,
			// -- pseud.last_name_hash1 as lastname1,
			// -- pseud.last_name_hash2 as lastname2,
			// -- pseud.last_name_hash3 as lastname3,
			// -- pseud.first_name_hash1 as firstname1,
			// -- pseud.first_name_hash2 as firstname2,
			// -- pseud.first_name_hash3 as firstname3,
			// -- pseud.birth_date_hash as birthdate1,
			// (case sb.sex when 1 then 'M' else 'F' end) as sex,
			// year(sb.birth_date) as birth_year,
			// st.id as study_id,
			// st.name as study_name,
			// dt.id as sequence_id,
			// -- dt_md.name as norm_sequence_name, 
			// -- dt_md.comment as sequence_name,
			// cnt.id as center_id, 
			// cnt.name as center,
			// man.name as device_manufacturer,
			// man_mod.name as device_model,
			// man_mod.magnetic_field as device_field_strength,
			// ac_eq.serial_number as device_serial_number,
			// ex.id as examination_id,
			// date(ex.examination_date) as examination_date,
			// pr_md.name as protocol_type

			// from studies.subject as sb
			// inner join studies.pseudonymus_hash_values as pseud
			// inner join studies.study as st
			// inner join studies.center as cnt
			// inner join studies.manufacturer as man
			// inner join studies.manufacturer_model as man_mod
			// inner join studies.acquisition_equipment as ac_eq
			// inner join datasets.mr_protocol as pr
			// inner join datasets.mr_protocol_metadata as pr_md
			// inner join datasets.examination as ex
			// inner join datasets.dataset as dt
			// -- inner join datasets.dataset_metadata as dt_md
			// inner join studies.subject_study as rel_sb_st
			// inner join datasets.dataset_acquisition as dt_acq
			// inner join datasets.mr_dataset_acquisition as mr_acq

			// on sb.pseudonymus_hash_values_id = pseud.id
			// and sb.id = rel_sb_st.subject_id
			// and rel_sb_st.study_id = st.id
			// and sb.id = ex.subject_id
			// and ex.center_id = cnt.id
			// and sb.id = dt.subject_id
			// and dt.dataset_acquisition_id = dt_acq.id
			// -- and dt_md.id = dt.updated_metadata_id
			// and dt_acq.acquisition_equipment_id = ac_eq.id
			// and ac_eq.manufacturer_model_id = man_mod.id
			// and man_mod.manufacturer_id = man.id
			// and dt_acq.id = mr_acq.id
			// and mr_acq.mr_protocol_id = pr.id
			// and pr_md.id = pr.id
			// and ex.id = dt_acq.examination_id;""");

			List<String> resuls = mrQuery.getResultList();

			FileOutputStream fos = new FileOutputStream(tmpFile);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			// FileWriter fw = new FileWriter("out.txt");
 
			for (String r : resuls) {
				bw.write(r);
				bw.newLine();
			}
		 
			bw.close();
			
		} catch (IOException e) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while querying the database.", e));
		}
		// Zip it
		File zipFile = new File(tmpFilePath + ZIP);
		zipFile.createNewFile();

		zip(tmpFile.getAbsolutePath(), zipFile.getAbsolutePath());

		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		FileUtils.deleteDirectory(tmpFile);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.contentLength(data.length)
				.body(resource);
	}
}
