package org.shanoir.challengeScores.controller;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.challengeScores.data.access.service.MetricService;
import org.shanoir.challengeScores.data.model.Metric;
import org.shanoir.challengeScores.data.model.mapping.swagerapi.MetricMapper;
import org.shanoir.challengeScores.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.api.MetricApiController;
import io.swagger.model.Metrics;

/**
 * Implement the logic for the generated Swagger server api : {@link MetricApiController}
 *
 * @author jlouis
 */
@Component
public class MetricApiDelegate {

	@Autowired
	private MetricService metricService;


	/**
	 * Constructor
	 */
	public MetricApiDelegate() {
	}


	/**
	 * Save a new metric.
	 * Implements the logic for the corresponding generated method :
	 * {@link MetricApiController#saveMetric(String, String, String, String)}
	 *
	 * @param name
	 * @param naN
	 * @param negInf
	 * @param posInf
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<io.swagger.model.Metric> saveMetric(String name, String naN, String negInf, String posInf, List<Long> studyIds) {
		Metric metric = new Metric(null, name, naN, negInf, posInf, studyIds);
		try {
			metricService.saveMetric(metric);
		} catch (DataIntegrityViolationException e) {
			return new ResponseEntity<io.swagger.model.Metric>(HttpStatus.CONFLICT);
		}
        return new ResponseEntity<io.swagger.model.Metric>(MetricMapper.modelToSwagger(metric), HttpStatus.CREATED);
	}


	/**
	 * Save or update a metric, depending the given id already exists in the database.
	 * Implements the logic for the corresponding generated method :
	 * {@link MetricApiController#createOrUpdateMetric(Long, String, String, String, String)}
	 *
	 * @param id
	 * @param name
	 * @param naN
	 * @param negInf
	 * @param posInf
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<Void> saveMetric(Long id, String name, String naN, String negInf, String posInf, List<Long> studyIds) {
		Metric metric = new Metric(id, name, naN, negInf, posInf, studyIds);
		boolean alreadyExistsInDatabase = metricService.getMetric(id) != null;
		try {
			metricService.saveMetric(metric);
		} catch (DataIntegrityViolationException e) {
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		}
		if (alreadyExistsInDatabase) {
			return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		}
	}


	/**
	 * Delete a metric.
	 * Implements the logic for the corresponding generated method :
	 * {@link MetricApiController#deleteMetricById(Long)}
	 *
	 * @param id
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<Void> deleteMetric(Long id) {
		try {
			metricService.deleteMetric(id);
		} catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	/**
	 * Get an existing metric.
	 * Implements the logic for the corresponding generated method :
	 * {@link MetricApiController#getMetricById(Long)}
	 *
	 * @param id
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<io.swagger.model.Metric> getMetric(Long id) {
		Metric metric = metricService.getMetric(id);
		if (metric != null) {
			return new ResponseEntity<io.swagger.model.Metric>(MetricMapper.modelToSwagger(metricService.getMetric(id)), HttpStatus.OK);
		} else {
			return new ResponseEntity<io.swagger.model.Metric>(HttpStatus.NOT_FOUND);
		}
	}


	/**
	 * Find every existing metric.
	 * Implements the logic for the corresponding generated method :
	 * {@link MetricsApiController#findAllMetrics()}
	 *
	 * @param id
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<List<io.swagger.model.Metric>> findAllMetrics() {
		List<io.swagger.model.Metric> metrics = MetricMapper.modelToSwagger(Utils.toList(metricService.findAll()));
		return new ResponseEntity<List<io.swagger.model.Metric>>(metrics, HttpStatus.OK);
	}


	/**
	 * Delete every metric.
	 * Implements the logic for the corresponding generated method :
	 * {@link MetricsApiController#deleteAllMetrics()}
	 *
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<Void> deleteAllMetrics() {
		metricService.deleteAll();
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	public ResponseEntity<Void> updateAll(Metrics swaggerMetrics) {
		List<Metric> metrics = new ArrayList<Metric>();
		for (io.swagger.model.Metric swaggerMetric : swaggerMetrics) {
			Metric metric = new Metric();
			metric.setId(swaggerMetric.getId().longValue());
			metric.setName(swaggerMetric.getName());
			metrics.add(metric);
		}
		//metricService.deleteAll();
		metricService.saveAll(metrics);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
}
