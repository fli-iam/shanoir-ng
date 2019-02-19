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

package io.swagger.api;

import java.util.List;

import org.shanoir.challengeScores.controller.MetricApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;
import io.swagger.model.Metric;
import io.swagger.model.Metrics;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

@Controller
public class MetricApiController implements MetricApi {

	@Autowired
	private MetricApiDelegate metricApiDelegate;

	public ResponseEntity<Void> createOrUpdateMetric(
			@ApiParam(value = "id of the metric", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "The name of the metric", required = true) @RequestParam(value = "name", required = true) String name,
			@ApiParam(value = "What to do if a value for this metric is NaN") @RequestParam(value = "naN", required = false) String naN,
			@ApiParam(value = "What to do if a value for this metric is a negative infinite") @RequestParam(value = "negInf", required = false) String negInf,
			@ApiParam(value = "What to do if a value for this metric a positive infinite") @RequestParam(value = "posInf", required = false) String posInf,
			@ApiParam(value = "ids of the involved studies") @RequestParam(value = "studyIds", required = false) List<Long> studyIds) {

		return metricApiDelegate.saveMetric(id, name, naN, negInf, posInf, studyIds);
	}

	public ResponseEntity<Void> deleteAllMetrics() {
        return metricApiDelegate.deleteAllMetrics();
    }

	public ResponseEntity<Void> deleteMetricById(@ApiParam(value = "id of the metric", required = true) @PathVariable("id") Long id) {
		return metricApiDelegate.deleteMetric(id);
	}

	public ResponseEntity<List<Metric>> findAllMetrics() {
		return metricApiDelegate.findAllMetrics();
	}

	public ResponseEntity<Metric> getMetricById(@ApiParam(value = "id of the metric", required = true) @PathVariable("id") Long id) {
		return metricApiDelegate.getMetric(id);
	}

	public ResponseEntity<Metric> saveMetric(
			@ApiParam(value = "The name of the metric", required = true) @RequestParam(value = "name", required = true) String name,
			@ApiParam(value = "What to do if a value for this metric is NaN") @RequestParam(value = "naN", required = false) String naN,
			@ApiParam(value = "What to do if a value for this metric is a negative infinite") @RequestParam(value = "negInf", required = false) String negInf,
			@ApiParam(value = "What to do if a value for this metric a positive infinite") @RequestParam(value = "posInf", required = false) String posInf,
			@ApiParam(value = "ids of the involved studies") @RequestParam(value = "studyIds", required = false) List<Long> studyIds) {

		return metricApiDelegate.saveMetric(name, naN, negInf, posInf, studyIds);
	}

	public ResponseEntity<Void> updateMetrics(@ApiParam(value = "the metrics to save", required = true) @RequestBody Metrics metrics) {
		return metricApiDelegate.updateAll(metrics);
	}

}
