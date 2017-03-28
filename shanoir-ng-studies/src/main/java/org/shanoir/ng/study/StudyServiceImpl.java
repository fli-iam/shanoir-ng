package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.shanoir.ng.shared.exception.ErrorModelCode;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyServiceImpl implements StudyService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(StudyServiceImpl.class);
	private EntityManager em;
	@Autowired
	private StudyRepository studyRepository;
	@Autowired
	private RelStudyUserRepository relStudyUserRepository;

	@Override
	public List<Study> findAll() {
		return studyRepository.findAll();
	}

	public Study createStudy(Study study) {

		Study newStudy = studyRepository.save(study);
		return newStudy;
	}

	@Override
	public Study update(Study study) {
		final Study studyDb = studyRepository.findOne(study.getId());
		studyDb.setName(study.getName());
		studyDb.setEndDate(study.getEndDate());
		studyDb.setClinical(study.isClinical());
		studyDb.setWithExamination(study.isWithExamination());
		studyDb.setVisibleByDefault(study.isVisibleByDefault());
		studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
		studyDb.setRefStudyStatus(study.getRefStudyStatus());

		studyRepository.save(studyDb);

		return studyDb;
	}

	@Override
	public void deleteById(Long id) throws ShanoirStudiesException {
		final Study study = studyRepository.findOne(id);
		if (study == null) {
			LOG.error("Study with id " + id + " not found");
			throw new ShanoirStudiesException(ErrorModelCode.USER_NOT_FOUND);
		}
		studyRepository.delete(id);

	}

	@Override
	public List<Study> findAllForUser(Long UserId) {

		List<Study> studyList = new ArrayList<Study>();

		for (RelStudyUser r : relStudyUserRepository.findAllByUserId(UserId)) {
			studyList.add(r.getStudy());
		}
		return studyList;
	}

	@Override
	public Study findById(Long id) {
			return studyRepository.findOne(id);
	}

}
