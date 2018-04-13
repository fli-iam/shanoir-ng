package org.shanoir.ng.subjectstudy;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
	public void deleteById(final Long id) throws ShanoirStudiesException {
		subjectStudyRepository.delete(id);
	}

	@Override
	public SubjectStudy save(final SubjectStudy subjectStudy) throws ShanoirStudiesException {
		SubjectStudy savedSubjectStudy = null;
		try {
			savedSubjectStudy = subjectStudyRepository.save(subjectStudy);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating subject study", dive);
			throw new ShanoirStudiesException("Error while creating subject study");
		}
		// updateShanoirOld(savedSubject);
		return savedSubjectStudy;
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
