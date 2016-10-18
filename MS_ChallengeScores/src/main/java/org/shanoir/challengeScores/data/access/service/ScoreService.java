package org.shanoir.challengeScores.data.access.service;

import org.shanoir.challengeScores.data.model.Score;

/**
 * Service interface for scores.
 *
 * @author jlouis
 *
 */
public interface ScoreService {

	void saveAll(Iterable<Score> scores);
	void deleteAll();
	void deleteAll(Iterable<Score> scores);
	Iterable<Score> getScore(Long studyId, Long ownerId, Long patientId, Long metricId);
	Iterable<Score> getScores(Long studyId);
}
