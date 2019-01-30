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
