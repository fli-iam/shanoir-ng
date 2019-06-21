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
