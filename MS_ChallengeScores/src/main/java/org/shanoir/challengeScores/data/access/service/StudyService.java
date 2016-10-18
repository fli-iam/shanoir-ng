package org.shanoir.challengeScores.data.access.service;

import org.shanoir.challengeScores.data.model.Study;

/**
 * Service interface for studies.
 *
 * @author jlouis
 *
 */
public interface StudyService {

	void save(Study study);
	void saveAll(Iterable<Study> studies);
	void deleteAll();
}
