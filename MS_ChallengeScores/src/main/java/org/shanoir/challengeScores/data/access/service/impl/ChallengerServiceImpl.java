/**
 *
 */
package org.shanoir.challengeScores.data.access.service.impl;

import org.shanoir.challengeScores.data.access.repository.ChallengerRepository;
import org.shanoir.challengeScores.data.access.service.ChallengerService;
import org.shanoir.challengeScores.data.model.Challenger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jlouis
 */
@Service
public class ChallengerServiceImpl implements ChallengerService {

	@Autowired
	private ChallengerRepository challengerRepository;

	@Override
	public Challenger find(Long id) {
		return challengerRepository.findOne(id);
	}

	@Override
	public void save(Challenger challenger) {
		challengerRepository.save(challenger);
	}

	@Override
	public void deleteAll() {
		challengerRepository.deleteAll();
	}

	@Override
	public void saveAll(Iterable<Challenger> challengers) {
		challengerRepository.save(challengers);
	}
}
