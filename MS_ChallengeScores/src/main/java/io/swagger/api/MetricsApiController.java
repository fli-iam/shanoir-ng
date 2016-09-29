package io.swagger.api;

import java.util.List;

import org.shanoir.challengeScores.controller.MetricApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import io.swagger.model.Metric;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-09-28T15:13:01.874Z")

@Controller
public class MetricsApiController implements MetricsApi {

	@Autowired
	private MetricApiDelegate metricApiDelegate;

    public ResponseEntity<List<Metric>> findAllMetrics() {

        return metricApiDelegate.findAllMetrics();
    }

}
