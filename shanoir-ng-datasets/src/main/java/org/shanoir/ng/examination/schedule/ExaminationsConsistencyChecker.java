package org.shanoir.ng.examination.schedule;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
	
	private final AtomicBoolean isTaskRunning = new AtomicBoolean(false);

//    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Run every 2 hours (in milliseconds)
	@Scheduled(fixedRate = 1000) // Run every 2 hours (in milliseconds)
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
				checkExamination(examination);
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
	
	private void checkExamination(Examination examination) {
		for (DatasetAcquisition acquisition : examination.getDatasetAcquisitions()) {
			for (Dataset dataset : acquisition.getDatasets()) {
				for (DatasetExpression expression : dataset.getDatasetExpressions()) {
					if (DatasetExpressionFormat.DICOM.equals(expression.getDatasetExpressionFormat())) {
						for (DatasetFile file : expression.getDatasetFiles()) {
							if (file.isPacs()) {
								String path = file.getPath();
								LOG.info(path);
							}
						}
					}
				}
			}
		}
	}

}
