package org.shanoir.ng.subject;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.model.ItemRepositoryCustom;

/**
 * Custom repository for subjects.
 * 
 * @author msimon
 *
 */
public interface SubjectRepositoryCustom extends ItemRepositoryCustom<Subject> {

	/**
	 * Find id and name for all studies.
	 * 
	 * @return list of studies.
	 */
	List<IdNameDTO> findIdsAndNames();
	
	
	/**
	 * Find subject by Id with subject study info (since it is a Lazy Loading).
	 * 
	 * @Param Long id;
	 * 
	 * @return Subject.
	 */
	Subject findSubjectWithSubjectStudyById(Long id);


}
