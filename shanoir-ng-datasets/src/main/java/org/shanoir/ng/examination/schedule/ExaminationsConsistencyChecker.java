package org.shanoir.ng.examination.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.WADOURLHandler;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * This class iterates over all examinations in the database of Shanoir and
 * applies multiple consistency checks on the data below in the tree. It
 * produces a .csv file with the result of his consistency check on the server.
 * It runs every two hours and stores the latest analyzed exam in its database.
 * 
 * The following is checked:
 * 
 * 1) It checks, if an examination is empty and has no data below.
 * 
 * 2) It checks the StudyInstanceUID per exam. Is it unique? Is there only one
 * StudyInstanceUID in all dataset files of the DICOM WADO path?
 * 
 * 3) Are all dataset files available in the PACS?
 * 
 * @author mkain
 *
 */
@Service
public class ExaminationsConsistencyChecker {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationsConsistencyChecker.class);

	@Autowired
	private ExaminationRepository examinationRepository;

	@Autowired
	private LatestCheckedExaminationRepository latestCheckedExaminationRepository;
	
	@Autowired
	private WADOURLHandler wadoURLHandler;
	
	private final AtomicBoolean isTaskRunning = new AtomicBoolean(false);

//    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Run every 2 hours (in milliseconds)
	@Scheduled(fixedRate = 10000) // Run every 2 hours (in milliseconds)
	@Transactional
	public void check() {
		if (!isTaskRunning.compareAndSet(false, true)) {
            return;
        }
		try {
			LOG.info("ExaminationsConsistencyChecker START...");
			LatestCheckedExamination latestCheckedExamination =
					latestCheckedExaminationRepository.findTopByOrderByIdDesc().orElse(null);
			List<Examination> examinationsToCheck;
			if (latestCheckedExamination != null) {
				examinationsToCheck = examinationRepository.findByIdGreaterThan(latestCheckedExamination.getExaminationId());
			} else {
				examinationsToCheck = examinationRepository.findAll();
			}
	
			for (Examination examination : examinationsToCheck) {
				LOG.info("Processing examination with ID: " + examination.getId());
				Set<String> studyInstanceUIDs = ConcurrentHashMap.newKeySet();
				List<String> filesInPACS = new ArrayList<String>();
				checkExamination(examination, filesInPACS);
				LOG.info("Examination {} references {} files in PACS.", examination.getId(), filesInPACS.size());
				filesInPACS.parallelStream().forEach(f -> {
					String studyInstanceUID = wadoURLHandler.extractUIDs(f)[0];
					studyInstanceUIDs.add(studyInstanceUID);
				});
				if (studyInstanceUIDs.size() > 1) {
					LOG.error("Examination {} contains multiple StudyInstanceUIDs ({}).", examination.getId(), studyInstanceUIDs.size());
				}
				if (latestCheckedExamination == null) {
					latestCheckedExamination = new LatestCheckedExamination();
				}
				latestCheckedExamination.setExaminationId(examination.getId());
				latestCheckedExaminationRepository.save(latestCheckedExamination);
			}
			LOG.info("ExaminationsConsistencyChecker STOP...");
		} catch(Exception e) {
			LOG.info("ExaminationsConsistencyChecker STOPPED with exception...");
			LOG.error(e.getMessage(), e);
		} finally {
			isTaskRunning.set(false);			
		}
	}
	
	private void checkExamination(Examination examination, List<String> filesInPACS) {
		List<DatasetAcquisition> acquisitions = examination.getDatasetAcquisitions();
		if (acquisitions != null && !acquisitions.isEmpty()) {
			acquisitions.stream().forEach(a -> {
				checkAcquisition(a, filesInPACS);
			});
		} else {
			LOG.info("Examination found without acquisitions: {}", examination.getId());
			List<String> extraDataFilePaths = examination.getExtraDataFilePathList();
			if (extraDataFilePaths != null && !extraDataFilePaths.isEmpty()) {
				// keep examination for extra data
			} else {
				// potentially delete empty examination later
			}
		}
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
