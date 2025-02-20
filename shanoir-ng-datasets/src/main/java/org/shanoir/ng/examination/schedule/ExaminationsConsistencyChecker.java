package org.shanoir.ng.examination.schedule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.Instant;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.WADOURLHandler;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import jakarta.transaction.Transactional;

/**
 * This class iterates over all examinations in the database of Shanoir and
 * applies multiple consistency checks on the DICOM data below in the tree. It
 * produces a .csv file with the result of his consistency check on the server.
 * It runs in an intervall and stores the latest analyzed exam in its database.
 * As ongoing imports can create temporarily empty examinations, we only check
 * on examinations older than yesterday, not from today.
 * 
 * StudyInstanceUIDs of examinations should be unique by default, but are not
 * unique in the database to cover the feature "copy datasets", where a clone
 * examination, should still point to the same DICOMs and therefore has the
 * same StudyInstanceUID.
 * 
 * The following is checked:
 * 
 * "ExaminationID", "ExaminationDate", "Today?", "Empty?", "#Files",
 * "StudyInstanceUID", "Multiple?", "Unique?"
 * 
 * Today - do not touch, in case ongoing import
 * #Files - number of dataset_files in pacs (only check dicoms)
 * Multiple - does the examination, after multiple imports contains multiple
 * 				StudyInstanceUIDs
 * Unique - is the StudyInstanceUID unique
 * 
 * @author mkain
 *
 */
@Service
public class ExaminationsConsistencyChecker {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationsConsistencyChecker.class);
	
	private static final String ECC_CSV = "ecc.csv";

	public static final int EXAMINATION_BATCH_SIZE = 100;

	@Value("${logging.file.name}")
    private String loggingFileName;

	@Autowired
	private ExaminationRepository examinationRepository;

	@Autowired
	private ExaminationLastCheckedRepository examinationLastCheckedRepository;
	
	@Autowired
	private WADOURLHandler wadoURLHandler;
	
	private final AtomicBoolean isTaskRunning = new AtomicBoolean(false);

//    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Run every 2 hours (in milliseconds)
	@Scheduled(fixedRate = 60000) // Run every 2 hours (in milliseconds)
	@Transactional
	public void check() {
		if (!isTaskRunning.compareAndSet(false, true)) {
            return;
        }
		try {
			LOG.info("---------------");
			LOG.info("---------------");
			LOG.info("START...");
			long startTime = System.currentTimeMillis();
			long examinationsCount = examinationRepository.countExaminations();
			LOG.info("Examinations count: " + examinationsCount);
			Examination lastExamination = null;
			ExaminationLastChecked examinationLastChecked =
					examinationLastCheckedRepository.findTopByOrderByIdDesc().orElse(null);
			if (examinationLastChecked != null) {
				lastExamination = examinationRepository.findById(examinationLastChecked.getExaminationId()).orElse(null);
				// in case last checked examination is not existing anymore (deletion in between)
				if (lastExamination == null) {
					LOG.info("Last checked examination has been deleted.");
				} else {
					LOG.info("Last checked examination has ID: " + lastExamination.getId());
				}
			}

			Map<Long, String> examinationIDToStudyInstanceUID = new LinkedHashMap<Long, String>();
			List<Long> emptyExaminations = new ArrayList<Long>();
			int pageNumber = 0;
			int totalExaminationsChecked = 0;
			List<Examination> examinationsToCheck;
			do {
				if (lastExamination != null) {
					examinationsToCheck = examinationRepository.findByIdGreaterThan(lastExamination.getId(), PageRequest.of(pageNumber, EXAMINATION_BATCH_SIZE)).getContent();
				} else {
					LOG.info("First run of ECC, start with first examination (or last checked deleted).");
					examinationsToCheck = examinationRepository.findAll(PageRequest.of(pageNumber, EXAMINATION_BATCH_SIZE)).getContent();
				}
				if (!examinationsToCheck.isEmpty()) {
					checkExaminations(examinationsToCheck, examinationLastChecked, examinationIDToStudyInstanceUID, emptyExaminations);
					totalExaminationsChecked += examinationsToCheck.size();
					lastExamination = examinationsToCheck.get(examinationsToCheck.size() - 1);
					pageNumber++;
				}				
			} while (!examinationsToCheck.isEmpty());
			
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			LOG.info("---------------");
			LOG.info("Summary: total examinations checked: " + totalExaminationsChecked);
			LOG.info("Summary: time required for entire check: " + duration + " milliseconds.");
			if (totalExaminationsChecked > 0) {
				LOG.info("Summary: average per examination: " + duration/totalExaminationsChecked + " milliseconds.");
			}
			LOG.info("Summary: number of empty examinations: " + emptyExaminations.size());
			LOG.info("STOP...");
			LOG.info("---------------");
			LOG.info("---------------");
		} catch(Exception e) {
			LOG.info("STOPPED with exception...");
			LOG.error(e.getMessage(), e);
		} finally {
			isTaskRunning.set(false);			
		}
	}

	private void checkExaminations(List<Examination> examinationsToCheck,
			ExaminationLastChecked examinationLastChecked, Map<Long, String> examinationIDToStudyInstanceUID,List<Long> emptyExaminations) throws IOException {
		if (!examinationsToCheck.isEmpty()) {
			File datasetsLogFile = new File(loggingFileName);
			if (datasetsLogFile.exists()) {
				File parent = datasetsLogFile.getParentFile();
				File csvFile = new File(parent.getAbsolutePath() + File.separator + ECC_CSV);
				try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
					if (!csvFile.exists()) {
						csvFile.createNewFile();
						String[] header = {"ExaminationID", "ExaminationDate", "Today?", "Empty?", "#Files", "StudyInstanceUID", "Multiple?", "Unique?"};
						writer.writeNext(header);
					}
					for (Examination examination : examinationsToCheck) {
						examinationLastChecked = checkExamination(examinationLastChecked,
							examination, writer, examinationIDToStudyInstanceUID, emptyExaminations);
					}
					// One insert is sufficient, only write at the end where it stopped
					examinationLastCheckedRepository.save(examinationLastChecked);
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			} else {
				LOG.error("Log file of datasets not found.");
			}
		} else {
			LOG.info("No new examinations found.");
		}
	}

	private ExaminationLastChecked checkExamination(ExaminationLastChecked examinationLastChecked,
			Examination examination, CSVWriter writer, Map<Long, String> examinationIDToStudyInstanceUID,List<Long> emptyExaminations) {
		LOG.debug("Processing examination with ID: " + examination.getId());
		long startTime = System.currentTimeMillis();
		String[] line = new String[8];
		line[0] = examination.getId().toString();
		line[1] = examination.getExaminationDate().toString();
		List<String> filesInPACS = new ArrayList<String>();
		boolean checked = checkExamination(examination, line, filesInPACS, emptyExaminations);
		if (checked) {
			line[3] = "0";
			LOG.debug("Examination {}: references {} files in PACS.", examination.getId(), filesInPACS.size());
			if (!filesInPACS.isEmpty()) {
				checkStudyInstanceUIDs(examination, filesInPACS, line, examinationIDToStudyInstanceUID, emptyExaminations);
			}
			if (examinationLastChecked == null) {
				examinationLastChecked = new ExaminationLastChecked();
			}
			examinationLastChecked.setExaminationId(examination.getId());
		} else {
			line[3] = "1";
		}
		line[4] = Integer.toString(filesInPACS.size());
		writer.writeNext(line);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        LOG.debug("Time required for examination check: " + duration + " milliseconds.");
		return examinationLastChecked;
	}

	private void checkStudyInstanceUIDs(Examination examination, List<String> filesInPACS, String[] line,
		Map<Long, String> examinationIDToStudyInstanceUID, List<Long> emptyExaminations) {
		Set<String> studyInstanceUIDs = ConcurrentHashMap.newKeySet();
		filesInPACS.parallelStream().forEach(f -> {
			String studyInstanceUID = wadoURLHandler.extractUIDs(f)[0];
			studyInstanceUIDs.add(studyInstanceUID);
		});
		if (studyInstanceUIDs.isEmpty()) {
			LOG.error("Examination {}: contains NULL StudyInstanceUIDs.");
		} else if (studyInstanceUIDs.size() > 1) {
			line[5] = "1";
			LOG.warn("Examination {}: contains multiple StudyInstanceUIDs ({}).", examination.getId(), studyInstanceUIDs.size());
			saveStudyInstanceUIDInCaseEmpty(examination, studyInstanceUIDs);
		} else {
			line[5] = "0";
			saveStudyInstanceUIDInCaseEmpty(examination, studyInstanceUIDs);
		}
		String studyInstanceUID = examination.getStudyInstanceUID();
		line[6] = studyInstanceUID;
		if (examinationIDToStudyInstanceUID.containsValue(studyInstanceUID)) {
			line[7] = "0";
		} else {
			line[7] = "1";
		}
		examinationIDToStudyInstanceUID.put(examination.getId(), examination.getStudyInstanceUID());
	}

	private void saveStudyInstanceUIDInCaseEmpty(Examination examination, Set<String> studyInstanceUIDs) {
		String studyInstanceUID = studyInstanceUIDs.iterator().next();
		if(examination.getStudyInstanceUID() != null && examination.getStudyInstanceUID().isBlank()) {
			examination.setStudyInstanceUID(studyInstanceUID);
			examinationRepository.save(examination);
			LOG.debug("Examination {}: StudyInstanceUID added in database: {}", examination.getId(), studyInstanceUID);
		} else {
			if (studyInstanceUID.equals(examination.getStudyInstanceUID())) {
				LOG.debug("Examination {}: has correct StudyInstanceUID in database: {}", examination.getId(), examination.getStudyInstanceUID());
			}
		}
	}
	
	private boolean checkExamination(Examination examination, String[] line, List<String> filesInPACS, List<Long> emptyExaminations) {
		List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
		if (acquisitions != null && !acquisitions.isEmpty()) {
			line[2] = "0";
			/**
			 * Ongoing imports can create empty examinations and fill them up later.
			 * To avoid confusion on this, we only check data from yesterday or older.
			 */
			DatasetAcquisition firstAcquisition = acquisitions.get(0);
			LocalDate importDate = firstAcquisition.getImportDate();
			// Old acquisitions in database do not have an importDate, so we go further,
			// and we avoid using examinations from today, in case still ongoing import
			if (importDate == null || !LocalDate.now().equals(importDate)) {
				acquisitions.stream().forEach(a -> {
					checkAcquisition(a, filesInPACS);
				});
			} else {
				LOG.info("Examination {} check stopped, as creation date today (avoid ongoing imports).", examination.getId());
				return false;
			}
		} else {
			line[2] = "1";
			emptyExaminations.add(examination.getId());
			LOG.warn("Examination found without acquisitions: {}", examination.getId());
			List<String> extraDataFilePaths = examination.getExtraDataFilePathList();
			if (extraDataFilePaths != null && !extraDataFilePaths.isEmpty()) {
				// keep examination for extra data
			} else {
				// potentially delete empty examination later
			}
		}
		return true;
	}
	
	private void checkAcquisition(DatasetAcquisition acquisition, List<String> filesInPACS) {
		List<Dataset> datasets = acquisition.getDatasets();
		if (datasets != null && !datasets.isEmpty()) {
			datasets.stream().forEach(d -> {
				checkDataset(d, filesInPACS);
			});
		} else {
			// potentially delete empty acquisition later
		}
	}

	private void checkDataset(Dataset dataset, List<String> filesInPACS) {
		List<DatasetExpression> expressions = dataset.getDatasetExpressions();
		if (expressions != null && !expressions.isEmpty()) {
			expressions.stream().forEach(e -> {
				if (DatasetExpressionFormat.DICOM.equals(e.getDatasetExpressionFormat())) {
					checkExpression(e, filesInPACS);
				}
			});
		} else {
			// potentially delete empty dataset later
		}
	}
	
	private void checkExpression(DatasetExpression expression, List<String> filesInPACS) {
		List<DatasetFile> files = expression.getDatasetFiles();
		if (files != null && !files.isEmpty()) {
			files.stream().forEach(f -> {
				if (f.isPacs()) {
					synchronized (filesInPACS) {
						filesInPACS.add(f.getPath());
					}
				}				
			});
		} else {
			// potentially delete empty expression later
		}
	}
	
}
