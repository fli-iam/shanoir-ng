package org.shanoir.ng.exporter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.shanoir.ng.dataset.DatasetDescription;
import org.shanoir.ng.dataset.controler.DatasetApiController.CoordinatesSystem;
import org.shanoir.ng.dataset.modality.EegDataSetDescription;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.Subject;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is a service class for BIDS purpose.
 * Create BIDS files.
 * Export Data in BIDS format at different levels: Study, Subject, Examination, Dataset.
 * In a possible future: import Data from BIDS format
 * @author JcomeD
 *
 */
@Service
@Scope("prototype")
public class BIDSService {
	
	private static final Logger LOG = LoggerFactory.getLogger(BIDSService.class);

	private static final String TASK = "_task_";

	private static final String ZIP = ".zip";

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";

	private static final String README_FILE = "README";

	private static final SecureRandom RANDOM = new SecureRandom();

	@Autowired
	private ExaminationService examinationService;

	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Returns data from the study formatted as BIDS in a .zip file.
	 * @param study the study we want to export as BIDS
	 * @return data from the study formatted as BIDS in a .zip file.
	 */
	public File exportAsBids(final Long studyId, final String studyName) {
		// Create source folder
		File baseDir = createBaseBidsFolder(studyName);
		
		// Iterate over subjects got from call to SubjectApiController.findSubjectsByStudyId() and get list of subjects

		List<Subject> subjs = getSubjectsForStudy(studyId);
		for (Subject subj : subjs) {
			exportAsBids(subj, studyName, baseDir);
		}

		return baseDir;
	}

	/**
	 * Get a list of subject from the study ID.
	 * @param studyId the study ID to get the subject for
	 * @return a list of users associated to the study
	 */
	private List<Subject> getSubjectsForStudy(final Long studyId) {
		HttpEntity<Object> entity = null;
		entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());

		// Request to study MS to get list of subjects related to study ID
		// With preclinical=null precise that we want ALL (preclinical and not) subjects
		ResponseEntity<Subject[]> response = null;
		try {
			response = restTemplate.exchange(
					microservicesRequestsService.getStudiesMsUrl() + MicroserviceRequestsService.SUBJECT + "/" + studyId +"/allSubjects?preclinical=null", HttpMethod.GET,
					entity, Subject[].class);
		} catch (RestClientException e) {
			LOG.error("Error on study microservice request - {}", e.getMessage());
		}

		List<Subject> subjects = new ArrayList<>();
		if (response != null) {
			if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.NO_CONTENT.equals(response.getStatusCode())) {
				subjects = Arrays.asList(response.getBody());
			} else {
				LOG.error("Error on study microservice response - status code: {}", response.getStatusCode());
			}
		}
		return subjects;
	}

	/**
	 * Create the study/BASE BIDS folder.
	 * @param studyName the study name
	 * @return the base folder newly created
	 */
	private File createBaseBidsFolder(final String studyName) {
		// 1. Create folder
		String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		long n = RANDOM.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}
		String tmpFilePath = tmpDir + File.separator + Long.toString(n);
		File workFolder = new File(tmpFilePath);
		workFolder.mkdirs();
		File zipFile = new File(tmpFilePath + ZIP);
		try {
			zipFile.createNewFile();
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

		// 2. Create dataset_description.json and README
		DatasetDescription datasetDescription = new DatasetDescription();
		datasetDescription.setName(studyName);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + DATASET_DESCRIPTION_FILE), datasetDescription);
			objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + README_FILE), studyName);
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		
		return workFolder;
	}

	/**
	 * Returns data from the subject formatted as BIDS in a .zip file.
	 * @param subject the subject we want to export as BIDS
	 * @param studyName the study name
	 * @param workDir Subject BIDS directory where we are working. Will be created if null.
	 * @return data from the subject formatted as BIDS in a .zip file.
	 */
	public File exportAsBids(final Subject subject, final String studyName, final File workDir) {
		// When workDir is not defined, we create the full BIDS export
		File baseDir;
		if (workDir == null) {
			baseDir = createBaseBidsFolder(studyName);
		} else {
			baseDir = workDir;
		}
		File subjDir = createSubjectFolder(subject.getName(), baseDir);

		// Get subject examinations and filter on the one with adapted study only
		final List<Examination> examinationList = examinationService.findBySubjectId(subject.getId());

		// Iterate over examinations to export them as BIDS
		for (Examination exam : examinationList) {
			exportAsBids(exam, subjDir, studyName, subject.getName());
		}
		return workDir == null ? baseDir : subjDir;
	}

	/**
	 * Create the subject/patient BIDS folder
	 * @param subjectName the subject name for which we want to create the folder
	 * @param baseDir the parent folder
	 * @return the newly created folder
	 */
	private File createSubjectFolder(final String subjectName, final File baseDir) {
		File subjectFolder = new File(baseDir.getAbsolutePath() + File.separator + "sub-" + subjectName);
		if (!subjectFolder.exists()) {
			subjectFolder.mkdirs();
		}
		return subjectFolder;
	}

	/**
	 * Returns data from the examination formatted as BIDS in a .zip file.
	 * @param examination the examination we want to export as BIDS
	 * @param workDir examination BIDS directory where we are working. Will be created if null.
	 * @param studyName the study name
	 * @param subjectName the subject name
	 * @return data from the examination formatted as BIDS in a .zip file.
	 */
	public File exportAsBids(final Examination examination, final File workDir, final String studyName, final String subjectName) {
		// When workDir is not defined, we create the full BIDS export
		File baseDir;
		File subjectDir;
		if (workDir == null) {
			baseDir = createBaseBidsFolder(studyName);
			subjectDir = createSubjectFolder(subjectName == null ? examination.getSubjectId().toString() : subjectName, baseDir);
		} else {
			baseDir = workDir.getParentFile();
			subjectDir = workDir;
		}
		File examDir = createExaminationFolder(examination, subjectDir);

		// Iterate over acquisitions/datasets
		for (DatasetAcquisition acq : examination.getDatasetAcquisitions()) {
			List<Dataset> datasets = acq.getDatasets();
			for (Dataset ds : datasets) {
				exportAsBids(ds, examDir, studyName, subjectName);
			}
		}

		return workDir == null ? baseDir : examDir;
	}

	/**
	 * Create the session/examination BIDS folder
	 * @param examination the examination for which we want to create the folder
	 * @param subjectDir the parent folder
	 * @return the newly created folder
	 */
	private File createExaminationFolder(final Examination examination, final File subjectDir) {
		File examFolder = new File(subjectDir.getAbsolutePath() + File.separator + "ses-" +  examination.getId());
		if (!examFolder.exists()) {
			examFolder.mkdirs();
		}
		return examFolder;
	}

	/**
	 * Returns data from the dataset formatted as BIDS in a .zip file.
	 * @param dataset the dataset we want to export as BIDS
	 * @param workDir Examination (session) bids directory where we are working. Will be created if null;
	 * @param studyName the study name
	 * @param subjectName the subject name
	 * @return data from the dataset formatted as BIDS in a .zip file.
	 */
	public File exportAsBids(final Dataset dataset, final File workDir, final String studyName, final String subjectName) {
		// When workDir is not defined, we create the full BIDS export
		File examDir;
		File baseDir;
		if (workDir == null) {
			baseDir = createBaseBidsFolder(studyName);
			File subjectDir = createSubjectFolder(subjectName == null ? dataset.getSubjectId().toString() : subjectName, baseDir);
			examDir = createExaminationFolder(dataset.getDatasetAcquisition().getExamination(), subjectDir);
		} else {
			baseDir = null;
			examDir = workDir;
		}
		// Create BIDS files for the dataset in the examination directory
		try {
			createDatasetBidsFiles(dataset, examDir, studyName, subjectName);
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

		return workDir == null ? baseDir : examDir;
	}

	/**
	 * Create the list of BIDS files associated to a dataset.
	 * @param dataset the dataset from which we want the specific BIDS files to be created
	 * @param workDir the working directory where files will be created
	 * @param studyName the study name
	 * @param subjectName the subject name
	 * @return A list of newly created specific BIDS files associated to the dataset in entry
	 * @throws IOException when we fail to create a file
	 */
	public void createDatasetBidsFiles(final Dataset dataset, final File workDir, final String studyName, final String subjectName) throws IOException {
		// Copy dataset files in the directory
		List<URL> pathURLs = new ArrayList<>();
		getDatasetFilePathURLs(dataset, pathURLs, null);

		for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
			URL url =  iterator.next();
			File srcFile = new File(url.getPath());
			File destFolder = new File(workDir.getAbsolutePath());

			Path pathToGo = Paths.get(destFolder.getAbsolutePath() + File.separator + srcFile.getName());
			try {
			Files.copy(srcFile.toPath(), pathToGo);
			} catch (IOException exception) {
				LOG.error("File could not be treated (PACS): {}", srcFile.getAbsolutePath(), exception);
			}
		}

		// Create specific files (EEG, MS, MEG, etc..)
		if (dataset instanceof EegDataset) {
			exportSpecificEegFiles((EegDataset) dataset, workDir, subjectName == null ? dataset.getSubjectId().toString() : subjectName, dataset.getDatasetAcquisition().getExamination().getId().toString(), studyName, dataset.getId().toString());
		} else if (dataset instanceof MrDataset) {
			// Do something specific about MR dataset
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
			if (format == null || datasetExpression.getDatasetExpressionFormat().equals(format)) {
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
	 * This methods export specific EEG files for BIDS export.
	 * - channel.tsv -> A list of channels from dataset.channels
	 * - event.tsv -> A list of events from dataset.events
	 * - [..]_eeg.json -> Description of EEG methods used
	 * - ? electrodes.tsv -> list of electrodes positions if existing
	 * - ? coordsystem.json -> if electrodes are defined, sets the reference
	 * See https://bids-specification.readthedocs.io/en/latest/04-modality-specific-files/03-electroencephalography.html
	 * for more informations
	 * @param dataset the dataset we want to export in BIDS
	 * @param workFolder the examination work folder in which we are working
	 * @param pathURLs list of file URL
	 * @param studyName the name of associated study
	 * @param subjectName the subject name associated
	 * @param sessionId the session ID / examination ID associated
	 * @param runId The run ID
	 * @throws RestServiceException
	 * @throws IOException
	 */
	private void exportSpecificEegFiles(final EegDataset dataset, final File workFolder, final String subjectName, final String sessionId, final String studyName, final String runId) throws IOException {
		// Create _eeg.json
		String fileName = "task_" + studyName + "_eeg.json";
		File baseDirectory = workFolder.getParentFile().getParentFile();
		String destFile = baseDirectory.getAbsolutePath() + File.separator + fileName;

		EegDataSetDescription datasetDescription = new EegDataSetDescription();
		datasetDescription.setTaskName(studyName);
		datasetDescription.setSamplingFrequency(String.valueOf(dataset.getSamplingFrequency()));
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(new File(destFile), datasetDescription);

		// Create channels.tsv file
		String destWorkFolderPath = baseDirectory.getAbsolutePath() + File.separator + "sub-" + subjectName + File.separator + "ses-" + sessionId + File.separator;
		
		// Create the folder where we are currently working if necessary.
		File destWorkFolderFile = new File(destWorkFolderPath);
		if (!destWorkFolderFile.exists()) {
			destWorkFolderFile.mkdirs();
		}

		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_channel.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		StringBuilder buffer = new StringBuilder();
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
		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_event.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuilder();
		buffer.append("onset \t duration \t sample \n");

		for (Event event: dataset.getEvents()) {
			float sample = Float.parseFloat(event.getPosition());
			float samplingFrequency = dataset.getSamplingFrequency();
			float onset = sample / samplingFrequency;
			int duration = event.getPoints();
			buffer.append(onset).append("\t")
			.append(duration == 0 ? "n/a" : String.valueOf(duration)).append("\t")
			.append(sample).append("\n");
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());

		// If no coordinates system, don't create electrode.csv & _coordsystem.json files
		if (dataset.getCoordinatesSystem() == null) {
			return;
		}

		// Create electrode.csv file
		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_electrodes.tsv";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuilder();
		buffer.append("name \t x \t y \t z \n");

		for (Channel chan: dataset.getChannels()) {
			buffer.append(chan.getName()).append("\t")
			.append(chan.getX()).append("\t")
			.append(chan.getY()).append("\t")
			.append(chan.getZ()).append("\n");
		}
		Files.write(Paths.get(destFile), buffer.toString().getBytes());
		
		// Create _coordsystem.json file
		fileName = subjectName + "_" + sessionId + TASK + studyName + "_" + runId + "_coordsystem.json";
		destFile = destWorkFolderPath + File.separator + fileName;

		buffer = new StringBuilder();
		buffer.append("{\n")
		.append("\"EEGCoordinateSystem\": ").append("\"" + dataset.getCoordinatesSystem()).append("\",\n")
		.append("\"EEGCoordinateUnits\": ").append("\"" +CoordinatesSystem.valueOf(dataset.getCoordinatesSystem()).getUnit()).append("\"\n")
		.append("}");
		
		Files.write(Paths.get(destFile), buffer.toString().getBytes());
	}

}
