package org.shanoir.ng.utils;

import java.util.Date;

import org.shanoir.ng.dataset.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.DatasetMetadata;
import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.studycard.StudyCard;

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

	// Study card data
	public static final String STUDY_CARD_NAME = "name";
	public static final Boolean STUDY_CARD_DISABLED = false;

	/**
	 * Create a CT dataset.
	 * 
	 * @return CT dataset.
	 */
	public CtDataset createCtDataset() {
		final CtDataset dataset = new CtDataset();
		dataset.setOriginMetadata(createDatasetSCMetadata());
		return dataset;
	}

	/**
	 * Create an MR dataset.
	 * 
	 * @return MR dataset.
	 */
	public static MrDataset createMrDataset() {
		final MrDataset dataset = new MrDataset();
		dataset.setStudyId(EXAMINATION_STUDY_ID);
		dataset.setOriginMetadata(createDatasetSCMetadata());
		return dataset;
	}

	/**
	 * Create a PET dataset.
	 * 
	 * @return PET dataset.
	 */
	public static PetDataset createPetDataset() {
		final PetDataset dataset = new PetDataset();
		dataset.setOriginMetadata(createDatasetSCMetadata());
		return dataset;
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
		examination.setPreclinical(false);
		return examination;
	}

	/**
	 * Create a template.
	 * 
	 * @return template.
	 */
	public static StudyCard createStudyCard() {
		final StudyCard studyCard = new StudyCard();
		studyCard.setName(STUDY_CARD_NAME);
		studyCard.setDisabled(STUDY_CARD_DISABLED);
		return studyCard;
	}

	/*
	 * Create an origin metadata for dataset.
	 * 
	 * @return metadata.
	 */
	private static DatasetMetadata createDatasetSCMetadata() {
		final DatasetMetadata metadata = new DatasetMetadata();
		metadata.setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET);
		metadata.setName(DATASET_NAME);
		return metadata;
	}

}
