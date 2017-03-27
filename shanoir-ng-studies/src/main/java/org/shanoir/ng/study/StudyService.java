package org.shanoir.ng.study;

import org.shanoir.ng.shared.exception.ShanoirStudyException;
import java.util.List;


public interface StudyService {

	/**
     * Get all the studies
     * @return a list of studies
     */
    List<Study> findAll();

    /**
     * add new study
     * @param study
     * @return
     */
    Study createStudy(Study study);

    /**
     *  Update a study
     * @param study
     * @return
     */
    Study update(Study study);


		/**
		 * Update a Study from the old Shanoir
		 *
		 * @param Study
		 *            Study.
		 * @throws ShanoirStudyException
		 */
		void updateFromShanoirOld(Study study) throws ShanoirStudyException;

}
