package org.shanoir.challengeScores.controller;

import org.shanoir.challengeScores.data.access.service.MetricService;
import org.shanoir.challengeScores.data.model.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller
 *
 * @author jlouis
 */
@Controller
public class SampleController {

	@Autowired
	private MetricService metricService;

    @RequestMapping("/")
    @ResponseBody
	String home() {
    	String test = "";
    	Iterable<Metric> metrics = metricService.findAll();
    	System.out.println(metrics);
    	for (Metric metric : metrics) {
    		System.out.println(metric.getName());
    		test += "<p>" + metric.getName() + "</p>";
    	}
		return test;
    }
}
