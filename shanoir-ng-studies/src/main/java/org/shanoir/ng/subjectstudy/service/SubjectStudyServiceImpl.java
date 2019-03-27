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
		return subjectStudyRepository.findOne(id);
	}

	@Override
	public SubjectStudy update(final SubjectStudy subjectStudy) throws EntityNotFoundException {
		final SubjectStudy subjectStudyDb = subjectStudyRepository.findOne(subjectStudy.getId());
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
