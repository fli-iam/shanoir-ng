/**
 *
 */
package org.shanoir.challengeScores.data.access.service.impl;

import org.shanoir.challengeScores.data.access.repository.StudyRepository;
import org.shanoir.challengeScores.data.access.service.StudyService;
import org.shanoir.challengeScores.data.model.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jlouis
 */
@Service
public class StudyServiceImpl implements StudyService {

	@Autowired
	private StudyRepository studyRepository;

	@Override
	public void save(Study study) {
		studyRepository.save(study);
	}

	@Override
	public void saveAll(Iterable<Study> studies) {
		studyRepository.save(studies);
	}

	@Override
	public void deleteAll() {
		studyRepository.deleteAll();
	}
}
