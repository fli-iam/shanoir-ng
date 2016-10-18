package org.shanoir.challengeScores.data.access.service;

import java.util.List;

import org.shanoir.challengeScores.data.model.Metric;

/**
 * Service interface for metrics.
 *
 * @author jlouis
 *
 */
public interface MetricService {

	Iterable<Metric> findAll();
	Iterable<Metric> findAll(Long studyId);
	void saveMetric(Metric metric);
	void saveAll(List<Metric> metrics);
	Metric getMetric(Long id);
	void deleteMetric(Long id);
	void deleteAll();
	Long getNextKey();
}
