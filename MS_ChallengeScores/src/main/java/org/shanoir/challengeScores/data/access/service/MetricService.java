package org.shanoir.challengeScores.data.access.service;

import org.shanoir.challengeScores.data.model.Metric;

/**
 * Service interface for metrics.
 *
 * @author jlouis
 *
 */
public interface MetricService {

	Iterable<Metric> findAll();
	void saveMetric(Metric metric);

}
