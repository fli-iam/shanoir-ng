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

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SubjectStudyServiceImpl.class);

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Override
	public SubjectStudy findById(final Long id) {
		return subjectStudyRepository.findOne(id);
	}

	@Override
	public SubjectStudy update(final SubjectStudy subjectStudy) throws ShanoirStudiesException {
		final SubjectStudy subjectStudyDb = subjectStudyRepository.findOne(subjectStudy.getId());
		updateSubjectStudyValues(subjectStudyDb, subjectStudy);
		try {
			subjectStudyRepository.save(subjectStudyDb);
		} catch (Exception e) {
			LOG.error("Error while updating subject", e);
			throw new ShanoirStudiesException("Error while updating subject");
		}
		//updateShanoirOld(subjectStudyDb);
		return subjectStudyDb;
	}
	
	/*
	 * Update some values of subject study to save them in database.
	 *
	 * @param subjectStudyDb subjectStudy found in database.
	 *
	 * @param subjectStudy subjectStudy with new values.
	 *
	 * @return database subjectStudy with new values.
	 */
	private SubjectStudy updateSubjectStudyValues(final SubjectStudy subjectStudyDb, final SubjectStudy subjectStudy) {

		subjectStudyDb.setId(subjectStudy.getId());
		subjectStudyDb.setPhysicallyInvolved(subjectStudy.isPhysicallyInvolved());
		subjectStudyDb.setSubjectStudyIdentifier(subjectStudy.getSubjectStudyIdentifier());
		subjectStudyDb.setSubjectType(subjectStudy.getSubjectType());
		return subjectStudyDb;
	}

	/* (non-Javadoc)
	 * @see org.shanoir.ng.shared.validation.UniqueCheckableService#findBy(java.lang.String, java.lang.Object)
	 */
	@Override
	public List<SubjectStudy> findBy(String fieldName, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

}
