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

package org.shanoir.ng.utils;

import java.util.ArrayList;

import org.apache.commons.lang3.RandomStringUtils;
import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.studycard.model.StudyCard;

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
		DatasetAcquisition dsa = new CtDatasetAcquisition();
		dsa.setId(1L);
		dataset.setDatasetAcquisition(dsa);
		return dataset;
	}

	/**
	 * Create an MR dataset.
	 * 
	 * @return MR dataset.
	 */
	public static MrDataset createMrDataset() {
		final MrDataset dataset = new MrDataset();
		dataset.setOriginMetadata(createDatasetSCMetadata());
		DatasetAcquisition dsa = createDatasetAcq();
		dsa.setId(1L);
		dataset.setDatasetAcquisition(dsa);
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
		DatasetAcquisition dsa = new PetDatasetAcquisition();
		dsa.setId(1L);
		dataset.setDatasetAcquisition(dsa);
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
		//examination.setExaminationDate(LocalDate.now());
		examination.setInvestigatorExternal(false);
		examination.setInvestigatorId(EXAMINATION_INVESTIGATOR_ID);
		examination.setNote(EXAMINATION_NOTE);
		examination.setStudyId(EXAMINATION_STUDY_ID);
		examination.setPreclinical(false);
		return examination;
	}
	
	/**
	 * Create an examination.
	 * 
	 * @return examination.
	 */
	public static DatasetAcquisition createDatasetAcq() {
		final DatasetAcquisition dsAcq = new MrDatasetAcquisition();
		dsAcq.setAcquisitionEquipmentId(1L);
		dsAcq.setDatasets(new ArrayList<Dataset>());
		dsAcq.setExamination(createExamination());
		dsAcq.getExamination().setId(1L);
		dsAcq.setRank(1);
		dsAcq.setSoftwareRelease("v1.0");
		dsAcq.setSortingIndex(1);
		dsAcq.setStudyCard(createStudyCard());
		dsAcq.getStudyCard().setId(1L);
		return dsAcq;
	}

	/**
	 * Create a template.
	 * 
	 * @return template.
	 */
	public static StudyCard createStudyCard() {
		final StudyCard studyCard = new StudyCard();
		studyCard.setName(STUDY_CARD_NAME + "_" + RandomStringUtils.randomAlphanumeric(5));
		studyCard.setDisabled(STUDY_CARD_DISABLED);
		studyCard.setStudyId(1L);
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
