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
