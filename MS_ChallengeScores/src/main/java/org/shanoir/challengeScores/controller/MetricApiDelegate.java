package org.shanoir.challengeScores.controller;

import org.shanoir.challengeScores.data.access.service.MetricService;
import org.shanoir.challengeScores.data.model.Metric;
import org.shanoir.challengeScores.data.model.mapping.swagerapi.MetricMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.api.MetricApiController;

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
	 * Implement the logic for the corresponding generated method :
	 * {@link MetricApiController#saveMetric(String, String, String, String)}
	 *
	 * @param name
	 * @param naN
	 * @param negInf
	 * @param posInf
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<io.swagger.model.Metric> saveMetric(String name, String naN, String negInf, String posInf) {
		Metric metric = new Metric();
		metric.setName(name);
		metric.setNaN(naN);
		metric.setNegInf(negInf);
		metric.setPosInf(posInf);
		metricService.saveMetric(metric);
        return new ResponseEntity<io.swagger.model.Metric>(MetricMapper.modelToSwagger(metric), HttpStatus.OK);
	}


}
