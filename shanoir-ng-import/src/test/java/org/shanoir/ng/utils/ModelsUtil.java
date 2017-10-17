package org.shanoir.ng.utils;

import org.shanoir.ng.examination.Examination;


/**
 * Utility class for test.
 * Generates models.
 * 
 * @author ifakhfakh
 *
 */
public final class ModelsUtil {
	
	private static final Long CENTER_ID = 1L;
	private static final String COMMENT = "examination 1";
	public static final String NOTE = "test examination";

	/**
	 * Create a center.
	 * 
	 * @return center.
	 */
	public static Examination createExamination() {
		final Examination examination = new Examination();
		
		examination.setCenterId(CENTER_ID);
		examination.setComment(COMMENT);
		examination.setInvestigatorExternal(false);
		examination.setNote(NOTE);
		return examination;
	}

}
