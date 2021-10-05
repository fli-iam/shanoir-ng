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

package org.shanoir.ng.subjectstudy.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SubjectStudy service implementation.
 *
 * @author yyao
 *
 */
@Service
public class SubjectStudyServiceImpl implements SubjectStudyService {

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Override
	public SubjectStudy findById(final Long id) {
		return subjectStudyRepository.findById(id).orElse(null);
	}

	@Override
	public SubjectStudy update(final SubjectStudy subjectStudy) throws EntityNotFoundException {
		final SubjectStudy subjectStudyDb = subjectStudyRepository.findById(subjectStudy.getId()).orElse(null);
		if (subjectStudyDb == null) throw new EntityNotFoundException(SubjectStudy.class, subjectStudy.getId());
		updateSubjectStudyValues(subjectStudyDb, subjectStudy);
		subjectStudyRepository.save(subjectStudyDb);
		return subjectStudyDb;
	}
	
	/*
	 * Update some values of subject study to save them in database.
	 *
	 * @param subjectStudyDb subjectStudy found in database.
	 * @param subjectStudy subjectStudy with new values.
	 * @return database subjectStudy with new values.
	 */
	private SubjectStudy updateSubjectStudyValues(final SubjectStudy subjectStudyDb, final SubjectStudy subjectStudy) {
		subjectStudyDb.setId(subjectStudy.getId());
		subjectStudyDb.setPhysicallyInvolved(subjectStudy.isPhysicallyInvolved());
		subjectStudyDb.setSubjectStudyIdentifier(subjectStudy.getSubjectStudyIdentifier());
		subjectStudyDb.setSubjectType(subjectStudy.getSubjectType());
		return subjectStudyDb;
	}

}
