package io.swagger.api;

import org.shanoir.challengeScores.controller.MetricApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;
import io.swagger.model.Metric;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-09-27T07:56:59.603Z")

@Controller
public class MetricApiController implements MetricApi {

	@Autowired
	private MetricApiDelegate metricApiDelegate;

    public ResponseEntity<Metric> saveMetric(@ApiParam(value = "The name of the metric", required = true) @RequestParam(value = "name", required = true) String name



,
        @ApiParam(value = "What to do if a value for this metric is NaN") @RequestParam(value = "naN", required = false) String naN



,
        @ApiParam(value = "What to do if a value for this metric is a negative infinite") @RequestParam(value = "negInf", required = false) String negInf



,
        @ApiParam(value = "What to do if a value for this metric a positive infinite") @RequestParam(value = "posInf", required = false) String posInf



) {
        return metricApiDelegate.saveMetric(name, naN, negInf, posInf);
    }

}
