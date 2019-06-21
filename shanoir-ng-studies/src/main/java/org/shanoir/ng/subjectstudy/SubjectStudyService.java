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

package org.shanoir.ng.subjectstudy;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Subject study service.
 *
 */
public interface SubjectStudyService extends UniqueCheckableService<SubjectStudy> {
	
	/**
	 * Find subject study by its id.
	 *
	 * @param id
	 *            subject study id.
	 * @return a subject study or null.
	 */
	SubjectStudy findById(Long id);
	
	/**
	 * Update subject study.
	 *
	 * @param subject study
	 *            subject study to update.
	 * @return updated subject study.
	 * @throws ShanoirStudiesException
	 */
	SubjectStudy update(SubjectStudy subjectStudy) throws ShanoirStudiesException;

}
