package org.shanoir.ng.utils;

import java.util.Date;

import org.shanoir.ng.dataset.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.examination.Examination;

/**
 * Utility class for test. Generates models.
 * 
 * @author msimon
 *
 */
public final class ModelsUtil {

	// Dataset data
	public static final String DATASET_NAME = "dataset";

	// Examination data
	private static final Long EXAMINATION_CENTER_ID = 1L;
	private static final String EXAMINATION_COMMENT = "examination 1";
	public static final Long EXAMINATION_INVESTIGATOR_ID = 1L;
	public static final String EXAMINATION_NOTE = "test examination";
	public static final Long EXAMINATION_STUDY_ID = 1L;

	/**
	 * Create a dataset.
	 * 
	 * @return dataset.
	 */
	public static Dataset createDataset() {
		final Dataset template = new Dataset();
		template.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);
		template.setName(DATASET_NAME);
		return template;
	}

	/**
	 * Create an examination.
	 * 
	 * @return examination.
	 */
	public static Examination createExamination() {
		final Examination examination = new Examination();
		examination.setCenterId(EXAMINATION_CENTER_ID);
		examination.setComment(EXAMINATION_COMMENT);
		examination.setExaminationDate(new Date());
		examination.setInvestigatorExternal(false);
		examination.setInvestigatorId(EXAMINATION_INVESTIGATOR_ID);
		examination.setNote(EXAMINATION_NOTE);
		examination.setStudyId(EXAMINATION_STUDY_ID);
		return examination;
	}

}
