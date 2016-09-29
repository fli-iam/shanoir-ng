package org.shanoir.challengeScores.data.model.mapping.swagerapi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.challengeScores.data.model.Metric;

public class MetricMapper {


	/**
	 * Convert {@link org.shanoir.challengeScores.data.model.Metric} to {@link io.swagger.model.Metric}
	 *
	 * @param metric  the source object
	 * @return the target object
	 */
	public static io.swagger.model.Metric modelToSwagger(Metric metric) {
		io.swagger.model.Metric swaggerMetric = new io.swagger.model.Metric();
		swaggerMetric.setId(new BigDecimal(metric.getId()));
		swaggerMetric.setName(metric.getName());
		swaggerMetric.setNaN(metric.getNaN());
		swaggerMetric.setNegInf(metric.getNegInf());
		swaggerMetric.setPosInf(metric.getPosInf());
		return swaggerMetric;
	}


	/**
	 * Convert {@link io.swagger.model.Metric} to {@link org.shanoir.challengeScores.data.model.Metric}
	 *
	 * @param metric  the source object
	 * @return the target object
	 */
	public static Metric swaggerToModel(io.swagger.model.Metric swaggerMetric) {
		Metric metric = new Metric();
		metric.setId(swaggerMetric.getId().longValue());
		metric.setName(swaggerMetric.getName());
		metric.setNaN(swaggerMetric.getNaN());
		metric.setNegInf(swaggerMetric.getNegInf());
		metric.setPosInf(swaggerMetric.getPosInf());
		return metric;
	}

	/**
	 * Convert a List of {@link io.swagger.model.Metric} to a list of {@link org.shanoir.challengeScores.data.model.Metric}
	 *
	 * @param metrics
	 * @return a list of the targeted class
	 */
	public static List<io.swagger.model.Metric> modelToSwagger(List<Metric> metrics) {
		List<io.swagger.model.Metric> swaggerMetrics = new ArrayList<io.swagger.model.Metric>();
		for (Metric metric : metrics) {
			swaggerMetrics.add(modelToSwagger(metric));
		}
		return swaggerMetrics;
	}

}
