package io.swagger.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.model.Metric;
import io.swagger.model.Metrics;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

@Api(value = "metric", description = "the metric API")
public interface MetricApi {

	@ApiOperation(value = "", notes = "Creates / updates a metric", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 201, message = "metric created", response = Void.class),
			@ApiResponse(code = 204, message = "metric updated", response = Void.class),
			@ApiResponse(code = 409, message = "metric name already exists in the database", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/metric/{id}", produces = { "application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> createOrUpdateMetric(
			@ApiParam(value = "id of the metric", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "The name of the metric", required = true) @RequestParam(value = "name", required = true) String name,
			@ApiParam(value = "What to do if a value for this metric is NaN") @RequestParam(value = "naN", required = false) String naN,
			@ApiParam(value = "What to do if a value for this metric is a negative infinite") @RequestParam(value = "negInf", required = false) String negInf,
			@ApiParam(value = "What to do if a value for this metric a positive infinite") @RequestParam(value = "posInf", required = false) String posInf,
			@ApiParam(value = "ids of the involved studies") @RequestParam(value = "studyIds", required = false) List<Long> studyIds);

	@ApiOperation(value = "", notes = "Deletes all metrics", response = Void.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "metrics cleared", response = Void.class),
        @ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
    @RequestMapping(value = "/metric/all",
        produces = { "application/json" },
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteAllMetrics();

	@ApiOperation(value = "", notes = "Deletes a single metric based on its id", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "metric deleted", response = Void.class),
			@ApiResponse(code = 404, message = "metric not found", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/metric/{id}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteMetricById(@ApiParam(value = "id of the metric", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "", notes = "Returns all the available metrics", response = Metric.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "all metrics", response = Metric.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Metric.class) })
	@RequestMapping(value = "/metric/all", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<Metric>> findAllMetrics();

	@ApiOperation(value = "", notes = "Gets a single metric based on its id", response = Metric.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "metric founded", response = Metric.class),
			@ApiResponse(code = 404, message = "metric not found", response = Metric.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Metric.class) })
	@RequestMapping(value = "/metric/{id}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Metric> getMetricById(@ApiParam(value = "id of the metric", required = true) @PathVariable("id") Long id);

	@ApiOperation(value = "", notes = "Creates new a metric", response = Metric.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 201, message = "metric created", response = Metric.class),
			@ApiResponse(code = 409, message = "metric name already exists in the database", response = Metric.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Metric.class) })
	@RequestMapping(value = "/metric", produces = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<Metric> saveMetric(
			@ApiParam(value = "The name of the metric", required = true) @RequestParam(value = "name", required = true) String name,
			@ApiParam(value = "What to do if a value for this metric is NaN") @RequestParam(value = "naN", required = false) String naN,
			@ApiParam(value = "What to do if a value for this metric is a negative infinite") @RequestParam(value = "negInf", required = false) String negInf,
			@ApiParam(value = "What to do if a value for this metric a positive infinite") @RequestParam(value = "posInf", required = false) String posInf,
			@ApiParam(value = "ids of the involved studies") @RequestParam(value = "studyIds", required = false) List<Long> studyIds);

	@ApiOperation(value = "", notes = "Updates the metric list", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "metrics updated", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/metric/all", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateMetrics(
			@ApiParam(value = "the metrics to save", required = true) @RequestBody Metrics metrics

	);
}
