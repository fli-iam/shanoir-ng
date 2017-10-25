package org.shanoir.ng.utils;

import org.shanoir.ng.dataset.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.Dataset;

/**
 * Utility class for test.
 * Generates models.
 * 
 * @author msimon
 *
 */
public final class ModelsUtil {

	// Dataset name
	public static final String DATASET_NAME = "dataset";
	
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
	
}
