package org.shanoir.challengeScores.data.access.service.impl;

import java.util.List;

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
	public Iterable<Metric> findAll(Long studyId) {
		return metricRepository.findByStudiesId(studyId);
	}

	@Override
	public void saveMetric(Metric metric) {
		metricRepository.save(metric);
	}

	@Override
	public Metric getMetric(Long id) {
		return metricRepository.findOne(id);
	}

	@Override
	public void deleteMetric(Long id) {
		metricRepository.delete(id);
	}

	@Override
	public void deleteAll() {
		metricRepository.deleteAll();
	}

	@Override
	public void saveAll(List<Metric> metrics) {
		metricRepository.save(metrics);
	}

	@Override
	public Long getNextKey() {
		Long max = new Long(0);
		for (Metric metric : metricRepository.findAll()) {
			if (metric.getId() > max) max = metric.getId();
		}
		return max + 1;
	}
}
