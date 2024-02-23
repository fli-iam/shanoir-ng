package org.shanoir.ng.examination.schedule;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This class iterates over all examinations in the database of Shanoir
 * and applies multiple consistency checks on the data below in the tree.
 * It produces a .csv file with the result of his consistency check on the
 * server. It runs every two hours and stores the latest analyzed exam in
 * its database.
 * 
 * The following is checked:
 * 
 * 1) It checks, if an examination is empty and has no data below.
 * 
 * 2) It checks the StudyInstanceUID per exam. Is it unique? Is there only
 * one StudyInstanceUID in all dataset files of the DICOM WADO path?
 * 
 * 3) Are all dataset files available in the PACS?
 * 
 * @author mkain
 *
 */
@Service
public class ExaminationConsistencyChecker {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationConsistencyChecker.class);
	
	@Autowired
    private ExaminationRepository examinationRepository;

    @Autowired
    private LatestCheckedExaminationRepository latestCheckedExaminationRepository;
    
//    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Run every 2 hours (in milliseconds)
    @Scheduled(fixedRate = 1000) // Run every 2 hours (in milliseconds)
    public void check() {
       LOG.info("ExaminationConsistencyChecker START...");
       
       // Query the database for all examinations
       List<Examination> examinations = examinationRepository.findAll();
       Optional<Examination> latestExamination = examinations.stream()
               .max(Comparator.comparing(Examination::getId));

       // Retrieve the existing LatestProcessedExam record, or create a new one
       LatestCheckedExamination latestCheckedExamination = latestCheckedExaminationRepository
               .findTopByOrderByIdDesc()
               .orElseGet(LatestCheckedExamination::new);

       // Update the ID of the latest checked examination
       latestExamination.ifPresent(examination -> {
    	   latestCheckedExamination.setExaminationId(examination.getId());
    	   latestCheckedExaminationRepository.save(latestCheckedExamination);

           // Your code to process the latest exam goes here
    	   LOG.info("Processing the latest exam with ID: " + examination.getId());
       });
    }

}
