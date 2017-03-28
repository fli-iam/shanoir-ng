package org.shanoir.ng.study;

import org.shanoir.ng.shared.exception.ShanoirStudyException;
import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;


public interface StudyService {

	/**
     * Get all the studies
     * @return a list of studies
     */
    List<Study> findAll();
    
    
    /**
	 * Find study by its id.
	 *
	 * @param id
	 *            study id.
	 * @return a study or null.
	 */
	Study findById(Long id);
	

    /**
     * add new study
     * @param study
     * @return created Study
     */
    Study createStudy(Study study);

    /**
     *  Update a study
     * @param study
     * @return updated study
     */
    Study update(Study study);
    
	/**
	 * Delete a Study
	 * 
	 * @param id
	 * @throws ShanoirStudiesException 
	 */
	void deleteById(Long id) throws  ShanoirStudiesException;
	
	/**
	 * Find all studies for a user
	 * @param id
	 * @return a list of studies
	 */
	List<Study> findAllForUser(Long UserId);



		/**
		 * Update a Study from the old Shanoir
		 *
		 * @param Study
		 *            Study.
		 * @throws ShanoirStudyException
		 */
		void updateFromShanoirOld(Study study) throws ShanoirStudyException;

}
