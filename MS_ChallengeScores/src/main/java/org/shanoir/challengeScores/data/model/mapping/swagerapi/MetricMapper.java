package org.shanoir.challengeScores.data.model.mapping.swagerapi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.challengeScores.data.model.Metric;
import org.shanoir.challengeScores.data.model.Study;

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
		swaggerMetric.setStudyIds(new ArrayList<Long>());
		for (Study study : metric.getStudies()) {
			swaggerMetric.addStudyIdsItem(study.getId());
		}
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
		metric.setStudies(new ArrayList<Study>());
		for (Long studyId : swaggerMetric.getStudyIds()) {
			metric.getStudies().add(new Study(studyId));
		}
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

	/**
	 * Convert a List of {@link org.shanoir.challengeScores.data.model.Metric} to a list of {@link io.swagger.model.Metric}
	 *
	 * @param metrics
	 * @return a list of the targeted class
	 */
	public static List<Metric> swaggerToModel(List<io.swagger.model.Metric> swaggerMetrics) {
		List<Metric> metrics = new ArrayList<Metric>();
		for (io.swagger.model.Metric swaggerMetric : swaggerMetrics) {
			metrics.add(swaggerToModel(swaggerMetric));
		}
		return metrics;
	}

}
