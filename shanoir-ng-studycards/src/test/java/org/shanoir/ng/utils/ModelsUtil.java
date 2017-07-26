package org.shanoir.ng.utils;

import org.shanoir.ng.studycard.StudyCard;

/**
 * Utility class for test.
 * Generates models.
 * 
 * @author msimon
 *
 */
public final class ModelsUtil {

	// Template data
	public static final String TEMPLATE_Name = "name";
	public static final Boolean TEMPLATE_DISABLES = false;
	
	/**
	 * Create a template.
	 * 
	 * @return template.
	 */
	public static StudyCard createStudyCard() {
		final StudyCard studyCard = new StudyCard();
		studyCard.setName(TEMPLATE_Name);
		studyCard.setDisabled(TEMPLATE_DISABLES);
		return studyCard;
	}
	
}
