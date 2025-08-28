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
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.subjectstudy.repository.SubjectStudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * SubjectStudy service implementation.
 *
 * @author yyao
 *
 */
@Service
public class SubjectStudyServiceImpl implements SubjectStudyService {

	private static final Logger LOG = LoggerFactory.getLogger(SubjectStudyServiceImpl.class);

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Autowired
	private SubjectService subjectService;
	
	@Override
	public SubjectStudy findById(final Long id) {
		return subjectStudyRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public SubjectStudy update(final SubjectStudy subjectStudyNew) throws EntityNotFoundException {
		final SubjectStudy subjectStudyOld = subjectStudyRepository.findById(subjectStudyNew.getId()).orElse(null);
		if (subjectStudyOld == null) throw new EntityNotFoundException(SubjectStudy.class, subjectStudyNew.getId());
		updateSubjectStudyValues(subjectStudyOld, subjectStudyNew);
		subjectStudyRepository.save(subjectStudyOld);
		return subjectStudyOld;
	}
	
	/*
	 * Update some values of subject study to save them in database.
	 *
	 * @param subjectStudyDb subjectStudy found in database.
	 * @param subjectStudy subjectStudy with new values.
	 * @return database subjectStudy with new values.
	 */
	private SubjectStudy updateSubjectStudyValues(final SubjectStudy subjectStudyOld, final SubjectStudy subjectStudyNew) {
		subjectStudyOld.setPhysicallyInvolved(subjectStudyNew.isPhysicallyInvolved());
		subjectStudyOld.setSubjectStudyIdentifier(subjectStudyNew.getSubjectStudyIdentifier());
		subjectStudyOld.setSubjectType(subjectStudyNew.getSubjectType());
		subjectService.mapSubjectStudyTagListToSubjectStudyTagList(subjectStudyOld, subjectStudyNew);
		return subjectStudyOld;
	}

}
