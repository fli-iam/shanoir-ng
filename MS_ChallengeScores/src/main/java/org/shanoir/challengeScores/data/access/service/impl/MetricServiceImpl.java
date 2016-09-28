package org.shanoir.challengeScores.data.access.service.impl;

import org.shanoir.challengeScores.data.access.repository.MetricRepository;
import org.shanoir.challengeScores.data.access.service.MetricService;
import org.shanoir.challengeScores.data.model.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for metrics.
 *
 * @author jlouis
 *
 */
@Service
public class MetricServiceImpl implements MetricService {

	@Autowired
	private MetricRepository metricRepository;

	@Override
	public Iterable<Metric> findAll() {
		return metricRepository.findAll();
	}

	@Override
	public void saveMetric(Metric metric) {
		metricRepository.save(metric);
	}



}
