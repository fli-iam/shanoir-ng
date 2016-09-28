package io.swagger.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.model.Metric;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-09-27T07:56:59.603Z")

@Api(value = "metric", description = "the metric API")
public interface MetricApi {

    @ApiOperation(value = "", notes = "Create a metric", response = Metric.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "metric created", response = Metric.class),
        @ApiResponse(code = 409, message = "Metric name already exists in the database", response = Metric.class),
        @ApiResponse(code = 200, message = "unexpected error", response = Metric.class) })
    @RequestMapping(value = "/metric",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Metric> saveMetric(@ApiParam(value = "The name of the metric", required = true) @RequestParam(value = "name", required = true) String name



,@ApiParam(value = "What to do if a value for this metric is NaN") @RequestParam(value = "naN", required = false) String naN



,@ApiParam(value = "What to do if a value for this metric is a negative infinite") @RequestParam(value = "negInf", required = false) String negInf



,@ApiParam(value = "What to do if a value for this metric a positive infinite") @RequestParam(value = "posInf", required = false) String posInf



);

}
