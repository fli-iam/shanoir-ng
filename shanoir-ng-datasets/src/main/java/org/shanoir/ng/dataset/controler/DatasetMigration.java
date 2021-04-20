package org.shanoir.ng.dataset.controler;

import java.util.List;

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.studycard.model.StudyCard;

/**
 * Move datasets to another shanoir server
 * @author JCome
 *
 */
public class DatasetMigration {
	
	private void moveDate() {
		// Get shanoir event
		
		// Get study ID
		
		// Move study cards
		StudyCard sc = new StudyCard();

		// to change
		sc.getId();
		sc.getStudyId();
		//sc.getRules();
		
		
		// Move examinations
		
		List<Examination> exams = null;
		// to reset
		exams.get(0).getDatasetAcquisitions();
		
		// to move
		exams.get(0).getExtraDataFilePathList();
		exams.get(0).getInstrumentBasedAssessmentList();
		
		// This will change
		exams.get(0).getSubjectId();
	}
}
