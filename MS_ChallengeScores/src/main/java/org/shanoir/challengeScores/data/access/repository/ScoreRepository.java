package org.shanoir.challengeScores.data.access.repository;

import org.shanoir.challengeScores.data.model.Score;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring repository interface.
 *
 * Allows to use basic CRUD methods on the parameterized type without even implementing this interface.
 * See {@link CrudRepository}.
 *
 * @author jlouis
 */
public interface ScoreRepository extends CrudRepository<Score, Long> {

	Iterable<Score> findByStudyIdAndOwnerIdAndPatientIdAndMetricId(Long studyId, Long ownerId, Long patientId, Long metricId);
	Iterable<Score> findByStudyId(Long studyId);

}
