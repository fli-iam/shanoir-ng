package org.shanoir.challengeScores.data.access.service.impl;

import org.shanoir.challengeScores.data.access.repository.ScoreRepository;
import org.shanoir.challengeScores.data.access.service.ScoreService;
import org.shanoir.challengeScores.data.model.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for metrics.
 *
 * @author jlouis
 *
 */
@Service
public class ScoreServiceImpl implements ScoreService {

	@Autowired
	private ScoreRepository scoreRepository;

	@Override
	public void saveAll(Iterable<Score> scores) {
		scoreRepository.save(scores);
	}

	@Override
	public Iterable<Score> getScore(Long studyId, Long ownerId, Long patientId, Long metricId) {
		return scoreRepository.findByStudyIdAndOwnerIdAndPatientIdAndMetricId(studyId, ownerId, patientId, metricId);
	}

	@Override
	public Iterable<Score> getScores(Long studyId) {
		return scoreRepository.findByStudyId(studyId);
	}

	@Override
	public void deleteAll(Iterable<Score> scores) {
		scoreRepository.delete(scores);
	}

	@Override
	public void deleteAll() {
		scoreRepository.deleteAll();
	}
}
