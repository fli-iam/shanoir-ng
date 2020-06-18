package org.shanoir.ng.exporter.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
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
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
public class BIDSServiceImpl implements BIDSService {
	
	private static final String TABULATION = "\t";

	private static final String NEW_LINE = "\n";

	private static final String SCANS_FILE_EXTENSION = "_scans.tsv";

	private static final String SESSION_PREFIX = "ses-";

	private static final String SUBJECT_PREFIX = "sub-";

	private static final String STUDY_PREFIX = "stud-";

	private static final Logger LOG = LoggerFactory.getLogger(BIDSServiceImpl.class);

	private static final String TASK = "_task_";

	private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";

	private static final String README_FILE = "README";

	@Value("${bids-data-folder}")
	private String bidsStorageDir;

	@Autowired
    private RabbitTemplate rabbitTemplate;

	@Autowired
	private ExaminationService examService;

	/**
	 * Returns data from the study formatted as BIDS in a .zip file.
	 * @param study the study we want to export as BIDS
	 * @return data from the study formatted as BIDS in a .zip file.
	 * @throws IOException
	 */
	@Override
	public File exportAsBids(final Long studyId, final String studyName) throws IOException {
		// Create source folder
		File baseDir = createBaseBidsFolder(studyName, studyId);
		
		// Iterate over subjects got from call to SubjectApiController.findSubjectsByStudyId() and get list of subjects
		List<Subject> subjs = getSubjectsForStudy(studyId);
		for (Subject subj : subjs) {
			exportAsBids(subj, studyName, baseDir);
		}

		return baseDir;
	}

	@Override
	public File addDataset(Examination exam, String subjectName, String studyName) throws IOException {
		// 0. If base file does not exist, create it from scratch
		File baseDir = new File(bidsStorageDir + File.separator + STUDY_PREFIX + exam.getStudyId() + "_" + studyName);
		if (!baseDir.exists()) {
			return exportAsBids(exam.getStudyId(), studyName);
		}

		// 1. Create Subject File if not existing
		File subjDir = createSubjectFolder(subjectName, exam.getSubjectId().toString(), baseDir);

		// 2. Create dataset files
		exportAsBids(exam, subjDir, studyName, subjectName);
		return baseDir;
	}

	@Override
	public void deleteDataset(Dataset dataset) {
		try {
			Long examId = dataset.getDatasetAcquisition().getExamination().getId();
			Long subjectId = dataset.getSubjectId();
			Long studyId = dataset.getStudyId();
	
			File fileToDelete = null;
			// Get study folder
			fileToDelete = getFileFromId(studyId.toString(), new File(bidsStorageDir));
			// Get subject folder
			File subjectFolder = getFileFromId(subjectId.toString(), fileToDelete);
			// Get exam folder
			fileToDelete = getFileFromId(examId.toString(), subjectFolder);
			// Get anat, eeg, [...] folder
			if (dataset instanceof EegDataset) {
				fileToDelete = getFileFromId("eeg", fileToDelete);
			} else if (dataset instanceof MrDataset) {
				fileToDelete = getFileFromId("anat", fileToDelete);
			}

			// Now delete only the data files we are interested in
			for (DatasetExpression expr : dataset.getDatasetExpressions()) {
				for (DatasetFile dataFile : expr.getDatasetFiles()) {
					if (!dataFile.isPacs()) {
						// Get FileName path object
				        Path path = Paths.get(dataFile.getPath());
				        Path fileName = path.getFileName();
						FileUtils.deleteQuietly(new File(fileToDelete + File.separator + fileName));

						// Delete from  scans.tsv searching by examination id / file name
						deleteLineFromFile(getScansFile(subjectFolder), dataset.getDatasetAcquisition().getExamination().getId(), fileName.toString());
					}
				}
			}
			
			if (fileToDelete == null || !fileToDelete.exists()) {
				return;
			}

			// And delete metadata files created for bids
			for (File metaDataFile : fileToDelete.listFiles()) {
				if (metaDataFile.getName() != null && metaDataFile.getName().contains("_" + dataset.getId() + "_")) {
					metaDataFile.delete();
				}
			}
			
		} catch (Exception e) {
			LOG.error("ERROR when deleting BIDS folder: please delete it manually: {}", e);
			e.printStackTrace();
		}
	}

	@Override
	public void deleteExam(Long examId) {
		Examination exam = examService.findById(examId);
		if (exam == null) {
			// Not found, just get back
			return;
		}

		File fileToDelete = null;
		try {
			// Get study folder
			fileToDelete = getFileFromId(exam.getStudyId().toString(), new File(bidsStorageDir));

			if (fileToDelete == null || !fileToDelete.exists()) {
				LOG.info("Trying to delete a non existing examination folder, file not deleted");
				return;
			}

			// Get subject folder
			fileToDelete = getFileFromId(exam.getSubjectId().toString(), fileToDelete);
			
			if (fileToDelete == null || !fileToDelete.exists()) {
				LOG.info("Trying to delete a non existing exmaination folder, file not deleted");
				return;
			}

			// delete from scans.tsv searching by examination ID
			File scans = getScansFile(fileToDelete);
			deleteLineFromFile(scans, examId, ".*");

			// Get exam folder
			fileToDelete = getFileFromId(examId.toString(), fileToDelete);


			// Delete all the folder
			FileUtils.deleteDirectory(fileToDelete);
		} catch (Exception e) {
			LOG.error("ERROR when deleting BIDS folder: please delete it manually: {}", fileToDelete, e);
		}
	}

	/**
	 * Deletes a line with given regex in the given file
	 * @param fileNameRegex the regex to find the filename to delete
	 * @param examId the examination ID
	 * @throws IOException
	 */
	private void deleteLineFromFile(File scansFile, Long examId, String fileNameRegex) throws IOException {
		File tempFile = new File(scansFile.getAbsolutePath() + "_tmp.tsv");

		BufferedReader reader = new BufferedReader(new FileReader(scansFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

		String currentLine;

		while((currentLine = reader.readLine()) != null) {
		    // trim newline when comparing with lineToRemove
		    String trimmedLine = currentLine.trim();
		    String[] columns = trimmedLine.split(TABULATION);
		    if(columns[2].equals(examId.toString())) {
		    	// Check filename regex
		        Pattern pattern = Pattern.compile(fileNameRegex);
		        Matcher matcher = pattern.matcher(columns[0]);
		    	if (matcher.find()) {
					continue;
				}
			}
		    writer.write(currentLine + System.getProperty("line.separator"));
		}
		writer.close();
		reader.close();
		tempFile.renameTo(scansFile);
	}

	/**
	 * Get a list of subject from the study ID.
	 * @param studyId the study ID to get the subject for
	 * @return a list of users associated to the study
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	private List<Subject> getSubjectsForStudy(final Long studyId) throws JsonParseException, JsonMappingException, IOException {

		/// Get the list of subjects
		String response = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.DATASET_SUBJECT_EXCHANGE, studyId);
		ObjectMapper objectMapper = new ObjectMapper();

		List<Subject> myObjects = objectMapper.readValue(response, new TypeReference<List<Subject>>(){});

		return myObjects;
	}

	/**
	 * Create the study/BASE BIDS folder.
	 * @param studyName the study name
	 * @param studyId the study id
	 * @return the base folder newly created
	 */
	private File createBaseBidsFolder(final String studyName, Long studyId) {
		// 1. Create folder
		String tmpFilePath = bidsStorageDir + File.separator + STUDY_PREFIX + studyId + '_' + studyName;
		File workFolder = new File(tmpFilePath);
		if (workFolder.exists()) {
			// If the file already exists, just return it
			return workFolder;
		}
		workFolder.mkdirs();

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
	 * Create all the data in a BIDS folder for a given subject
	 * @param subject the subject we want to export as BIDS
	 * @param studyName the study name
	 * @param workDir Subject BIDS directory where we are working. Will be created if null.
	 * @return data from the subject formatted as BIDS in a .zip file.
	 * @throws IOException
	 */
	private void exportAsBids(final Subject subject, final String studyName, final File workDir) throws IOException {
		File subjDir = createSubjectFolder(subject.getName(), String.valueOf(subject.getId()), workDir);

		// Get subject examinations and filter on the one with adapted study only
		final List<Examination> examinationList = examService.findBySubjectId(subject.getId());

		// Iterate over examinations to export them as BIDS
		for (Examination exam : examinationList) {
			// OTHER: can we imagine a subject in multiple studies ? Do the filter here
			exportAsBids(exam, subjDir, studyName, subject.getName());
		}
	}

	/**
	 * Create the subject/patient BIDS folder
	 * @param subjectName the subject name for which we want to create the folder
	 * @param baseDir the parent folder
	 * @param subjectId the subject id
	 * @return the newly created folder
	 * @throws IOException
	 */
	private File createSubjectFolder(final String subjectName, final String subjectId, final File baseDir) throws IOException {
		File subjectFolder = new File(baseDir.getAbsolutePath() + File.separator + SUBJECT_PREFIX + subjectId + "_" + subjectName);
		if (!subjectFolder.exists()) {
			subjectFolder.mkdirs();
		}
		getScansFile(subjectFolder);
		return subjectFolder;
	}

	/**
	 * Returns data from the examination formatted as BIDS in a .zip file.
	 * @param examination the examination we want to export as BIDS
	 * @param subjDir examination BIDS directory where we are working.
	 * @param studyName the study name
	 * @param subjectName the subject name
	 * @return data from the examination formatted as BIDS in a .zip file.
	 * @throws IOException
	 */
	private void exportAsBids(final Examination examination, final File subjDir, final String studyName, final String subjectName) throws IOException {
		File examDir = createExaminationFolder(examination, subjDir);

		// Iterate over acquisitions/datasets
		for (DatasetAcquisition acq : examination.getDatasetAcquisitions()) {
			List<Dataset> datasets = acq.getDatasets();
			for (Dataset ds : datasets) {
				try {
					createDatasetBidsFiles(ds, examDir, studyName, subjectName);
				} catch (IOException e) {
					LOG.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * Create the session/examination BIDS folder
	 * @param examination the examination for which we want to create the folder
	 * @param subjectDir the parent folder
	 * @return the newly created folder
	 */
	private File createExaminationFolder(final Examination examination, final File subjectDir) {
		File examFolder = new File(subjectDir.getAbsolutePath() + File.separator + SESSION_PREFIX +  examination.getId());
		if (!examFolder.exists()) {
			examFolder.mkdirs();
		}
		return examFolder;
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
	private void createDatasetBidsFiles(final Dataset dataset, final File workDir, final String studyName, final String subjectName) throws IOException {
		File dataFolder;

		// Create specific files (EEG, MS, MEG, etc..)
		if (dataset instanceof EegDataset) {
			dataFolder = createDataFolder("eeg", workDir);
			exportSpecificEegFiles((EegDataset) dataset, workDir, subjectName, dataset.getDatasetAcquisition().getExamination().getId().toString(), studyName, dataset.getId().toString());
		} else if (dataset instanceof MrDataset) {
			// Do something specific about MR dataset
			dataFolder = createDataFolder("anat", workDir);
		} else {
			dataFolder = workDir;
		}
		// Copy dataset files in the directory AS hard link to avoid duplicating files
		List<URL> pathURLs = new ArrayList<>();
		getDatasetFilePathURLs(dataset, pathURLs, null);

		for (Iterator<URL> iterator = pathURLs.iterator(); iterator.hasNext();) {
			URL url =  iterator.next();
			File srcFile = new File(url.getPath());

			Path pathToGo = Paths.get(dataFolder.getAbsolutePath() + File.separator + srcFile.getName());
			try {
				// Use link to avoid file duplication
				Files.createLink(pathToGo, srcFile.toPath());
				
				// Add the file to the scans.tsv reference
				File scansTsvFile = getScansFile(workDir.getParentFile());
				StringBuilder buffer = new StringBuilder();
				buffer.append(pathToGo.getFileName()).append(TABULATION)
					.append(dataset.getDatasetAcquisition().getExamination().getExaminationDate()).append(TABULATION)
					.append(dataset.getDatasetAcquisition().getExamination().getId())
					.append(NEW_LINE);

				// TODO: center_id / comment / weigth / other examination things ?
				Files.write(Paths.get(scansTsvFile.getAbsolutePath()), buffer.toString().getBytes(), StandardOpenOption.APPEND);

			} catch (Exception exception) {
				exception.printStackTrace();
				LOG.error("File could not be treated: {}", srcFile.getAbsolutePath(), exception);
			}
		}
	}

	private File getScansFile(File parentFile) throws IOException {
		// What if we don't have subject name ?
		File scansFile = new File(parentFile.getAbsolutePath() + File.separator + parentFile.getName() + SCANS_FILE_EXTENSION);
		if (!scansFile.exists()) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("filename").append(TABULATION)
				.append("acq_time").append(TABULATION)
				.append("session_id")
				.append(NEW_LINE);
			// TODO: center_id / comment / weigth / other examination things ?
			Files.write(Paths.get(scansFile.getAbsolutePath()), buffer.toString().getBytes());
		}
		return scansFile;
	}

	/**
	 * Create /eeg, /func ,[...] folder in BIDS file if not existing.
	 * @return the folder newly created.
	 */
	private File createDataFolder(final String folderName, final File workDir) {
		File dataFolder = new File(workDir.getAbsolutePath() + File.separator + folderName);
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		return dataFolder;
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
					if (!datasetFile.isPacs()) {
						URL url = new URL(datasetFile.getPath().replaceAll("%20", " "));
						pathURLs.add(url);
					}
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
		String destWorkFolderPath = baseDirectory.getAbsolutePath() + File.separator + SUBJECT_PREFIX + dataset.getSubjectId() + "_" + subjectName + File.separator + SESSION_PREFIX + sessionId + File.separator + "eeg" + File.separator;

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
			buffer.append(chan.getName()).append(TABULATION)
			.append(chan.getReferenceType().name()).append(TABULATION)
			.append(chan.getReferenceUnits()).append(TABULATION)
			.append(dataset.getSamplingFrequency()).append(TABULATION)
			.append(chan.getLowCutoff() == 0 ? "n/a" : chan.getLowCutoff()).append(TABULATION)
			.append(chan.getHighCutoff() == 0 ? "n/a" : chan.getHighCutoff()).append(TABULATION)
			.append(chan.getNotch() == 0 ? "n/a" : chan.getNotch()).append(NEW_LINE);
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
			buffer.append(onset).append(TABULATION)
			.append(duration == 0 ? "n/a" : String.valueOf(duration)).append(TABULATION)
			.append(sample).append(NEW_LINE);
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
			buffer.append(chan.getName()).append(TABULATION)
			.append(chan.getX()).append(TABULATION)
			.append(chan.getY()).append(TABULATION)
			.append(chan.getZ()).append(NEW_LINE);
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

	/**
	 * This method allows to find a specific object ( Study or subject ) from its ID in the given folder
	 * @param id the ID to find, or the modality
	 * @param folder the folder where we are looking for.s
	 * @return The File we found, null otherwise
	 * @throws IOException when there is a duplicated folder
	 */
	protected File getFileFromId(String id, File folder) throws IOException {
		if (!folder.exists()) {
			throw new IOException("ERROR: parent folder does not exist:" + folder.getAbsolutePath());
		}
		File[] files = folder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(SUBJECT_PREFIX + id + "_") && !name.endsWith(".zip")  && !name.endsWith(".tsv")
						|| name.startsWith(STUDY_PREFIX + id + "_")
						|| name.equals(SESSION_PREFIX + id)
						|| name.equals(id);
			}
		});
		if (files.length == 1) {
			return files[0];
		} else if (files.length > 1) {
			LOG.error("ERROR: duplicate folder containing ID: {} in bids folder", id);
			throw new IOException("ERROR: duplicate folder containing ID:" + id + "{} in bids folder");
		}
		LOG.info("ERROR: no folder containing ID: {} in bids folder. It will probably be created by the BIDS manager. Should not happen", id);
		return null;
	}
}
