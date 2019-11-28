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
import java.nio.file.OpenOption;
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
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDataSetDescription;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDatasetNature;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
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

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetApiController implements DatasetApi {

	private static final String ZIP = ".zip";

	private static final String DOWNLOAD = ".download";

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
	private EegDatasetMapper eegDatasetMapper;

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
		else if (dataset instanceof EegDataset) {
			return new ResponseEntity<>(eegDatasetMapper.datasetToDatasetDTO((EegDataset) dataset), HttpStatus.OK);
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
			@ApiParam(value = "Decide if you want to download dicom (dcm) or nifti (nii) files.", allowableValues = "dcm, nii, eeg", defaultValue = "dcm") 
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
			} else if ("eeg".equals(format)) {
				getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
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
			DatasetDescription datasetDescription = new DatasetDescription();
			datasetDescription.setName(studyName);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + DATASET_DESCRIPTION_FILE), datasetDescription);
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + README_FILE), studyName);

			for (Examination examination: examinationList) {
				final List<DatasetAcquisition> datasetAcquisitionList = examination.getDatasetAcquisitions();
				for (DatasetAcquisition datasetAcquisition : datasetAcquisitionList) {
					final List<Dataset> datasetList = datasetAcquisition.getDatasets();
					for (Dataset dataset: datasetList) {
						// NB: Only for EEG for the moment
						if (dataset instanceof EegDataset) {
							String runId = dataset.getId().toString();
							String sesLabel;
							// If there is only one examination, but multiple datasets, split sessions by datasets
							// If there is multiple examinations, split sessions by examinations
							if (examinationList.size() == 1) {
								sesLabel = runId;
							} else {
								sesLabel = examination.getId().toString();
							}
							List<URL> pathURLs = new ArrayList<URL>();
							getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.EEG);
							exportSpecificEegFiles(((EegDataset)dataset), workFolder, pathURLs, subjectName, sesLabel, studyName, runId );
						}
					}
				}
			}

			// 8. Get modality label, nii and json of dataset
			/* For the demo: one exam, one acq, one dataset, one modality which is T1 */
			final Dataset dataset = examinationList.get(0).getDatasetAcquisitions().get(0).getDatasets().get(0);
			if (dataset == null) {
				throw new RestServiceException(
						new ErrorModel(HttpStatus.NOT_FOUND.value(), "No Dataset found for subject Id.", null));
			}

			// Get modality label
			String modalityLabel = "";
			if (dataset instanceof MrDataset) {
				if (MrDatasetNature.T1_WEIGHTED_MR_DATASET.equals(((MrDataset) dataset).getUpdatedMrMetadata().getMrDatasetNature())
						|| MrDatasetNature.T1_WEIGHTED_DCE_MR_DATASET.equals(((MrDataset) dataset).getUpdatedMrMetadata().getMrDatasetNature())) {
					modalityLabel = T1w;
				} 
				if (StringUtils.isEmpty(modalityLabel)) {
					throw new RestServiceException(
							new ErrorModel(HttpStatus.NOT_FOUND.value(), "No MrDatasetNature, so could not define modality label and export BIDS!", null));
				}
			}

			// Get nii and json files
			else {
				try {
					List<URL> pathURLs = new ArrayList<URL>();
					getDatasetFilePathURLs(dataset, pathURLs, DatasetExpressionFormat.NIFTI_SINGLE_FILE);
					copyFilesForBIDSExport(pathURLs, workFolder, subjectName, examinationList.get(0).getId().toString(), modalityLabel);
				} catch (IOException e) {
					throw new RestServiceException(
							new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error exporting nifti files for subject in BIDS.", null));
				}
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
	 * This methods export specific EEG files for BIDS export.
	 * - channel.tsv -> A list of channels from dataset.channels
	 * - event.tsv -> A list of events from dataset.events
	 * - [..]_eeg.json -> Description of EEG methods used
	 * - ? electrodes.tsv -> list of electrodes positions if existing
	 * - ? coordsystem.json -> if electrodes are defined, sets the reference
	 * See https://bids-specification.readthedocs.io/en/latest/04-modality-specific-files/03-electroencephalography.html
	 * for more informations
	 * @param dataset the dataset we want to export in BIDS
	 * @param workFolder the work folder in which we are working
	 * @param pathURLs list of file URL
	 * @param studyName the name of associated study
	 * @param subjectName the subject name associated
	 * @param sessionId the session ID / examination ID associated
	 * @param runId The run ID
	 * @throws RestServiceException 
	 * @throws IOException 
	 */
	private void exportSpecificEegFiles(EegDataset dataset, File workFolder, List<URL> pathURLs, String subjectName, String sessionId, String studyName, String runId) throws RestServiceException, IOException {
		// Create _eeg.json
		String fileName = "task_" + studyName + "_eeg.json";
		String destFile = workFolder + File.separator + fileName;

		EegDataSetDescription datasetDescription = new EegDataSetDescription();
		datasetDescription.setTaskName(studyName);
		datasetDescription.setSamplingFrequency(String.valueOf(dataset.getSamplingFrequency()));
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(new File(destFile), datasetDescription);

		// Create channels.tsv file
		String destWorkFolderPath = workFolder.getAbsolutePath() + File.separator + "sub-" + subjectName + File.separator + "ses-" + sessionId + File.separator;
		
		// Create the folder where we are currently working
		new File(destWorkFolderPath).mkdirs();
		
		fileName = subjectName + "_" + sessionId + "_task_" + studyName + "_" + runId + "_channel.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		StringBuffer buffer = new StringBuffer();
		buffer.append("name \t type \t units \t sampling_frequency \t low_cutoff \t high_cutoff \t notch \n");

		for (Channel chan: dataset.getChannels()) {
			buffer.append(chan.getName()).append("\t")
			.append(chan.getReferenceType().name()).append("\t")
			.append(chan.getReferenceUnits()).append("\t")
			.append(dataset.getSamplingFrequency()).append("\t")
			.append(chan.getLowCutoff() == 0 ? "n/a" : chan.getLowCutoff()).append("\t")
			.append(chan.getHighCutoff() == 0 ? "n/a" : chan.getHighCutoff()).append("\t")
			.append(chan.getNotch() == 0 ? "n/a" : chan.getNotch()).append("\n");
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());
		
		// Create events.tsv file
		fileName = sessionId + "_" + sessionId + "_task_" + studyName + "_" + runId + "_event.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuffer();
		buffer.append("onset \t duration \t sample \n");

		for (Event event: dataset.getEvents()) {
			float sample = Float.valueOf(event.getPosition());
			float samplingFrequency = dataset.getSamplingFrequency();
			float onset = sample / samplingFrequency;
			int duration = event.getPoints();
			buffer.append(onset).append("\t")
			.append(duration == 0 ? "n/a" : String.valueOf(duration)).append("\t")
			.append(sample).append("\n");
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());

		// Copy files
		for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
			URL url =  (URL) iterator.next();
			File srcFile = new File(url.getPath());
			File destFolder = new File(destWorkFolderPath);

			Path pathToGo = Paths.get(destFolder.getAbsolutePath() + File.separator + srcFile.getName());
			Files.copy(srcFile.toPath(), pathToGo);
		}
	
		// If no coordinates system, don't create electrode.csv & _coordsystem.json files
		if (dataset.getCoordinatesSystem() == null) {
			return;
		}

		// Create electrode.csv file
		fileName = subjectName + "_" + sessionId + "_task_" + studyName + "_" + runId + "_electrodes.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuffer();
		buffer.append("name \t x \t y \t z \n");

		for (Channel chan: dataset.getChannels()) {
			buffer.append(chan.getName()).append("\t")
			.append(chan.getX()).append("\t")
			.append(chan.getY()).append("\t")
			.append(chan.getZ()).append("\n");
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());
		
		// Create _coordsystem.json file
		fileName = subjectName + "_" + sessionId + "_task_" + studyName + "_" + runId + "_coordsystem.json";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuffer();
		buffer.append("{\n")
		.append("\"EEGCoordinateSystem\": ").append("\"" + dataset.getCoordinatesSystem()).append("\",\n")
		.append("\"EEGCoordinateUnits\": ").append("\"" +CoordinatesSystem.valueOf(dataset.getCoordinatesSystem()).getUnit()).append("\"\n")
		.append("}");
		
		Files.write(Paths.get(destFile), buffer.toString().getBytes());
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
	
	/**
	 * This enum is for coordinates system and associated units
	 */
	public enum CoordinatesSystem {
	    ACPC("mm"),
	    Allen("mm"),
	    Analyze("mm"),
	    BTi_4D("m"),
	    CTF_MRI("mm"),
	    CTF_gradiometer("cm"),
	    CapTrak("mm"),
	    Chieti("mm"),
	    DICOM("mm"),
	    FreeSurfer("mm"),  
	    MNI("mm"),
	    NIfTI("mm"),
	    Neuromag_Elekta("m"),
	    Paxinos_Franklin("mm"),
	    Talairach_Tournoux("mm"),
	    Yokogawa("n/a");
	    
	    private String unit;
	    
	    CoordinatesSystem(String pUnit) {
	    	this.unit = pUnit;
	    }
	    public String getUnit() {
	    	return unit;
	    }
	}
	
}
