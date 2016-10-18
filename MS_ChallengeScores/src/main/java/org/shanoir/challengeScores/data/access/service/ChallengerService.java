package org.shanoir.challengeScores.data.access.service;

import org.shanoir.challengeScores.data.model.Challenger;

/**
 * Service interface for challengers.
 *
 * @author jlouis
 *
 */
public interface ChallengerService {

	Challenger find(Long id);
	void save(Challenger challenger);
	void saveAll(Iterable<Challenger> challengers);
	void deleteAll();
}
