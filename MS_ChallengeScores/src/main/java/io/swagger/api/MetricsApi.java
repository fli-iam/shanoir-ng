package io.swagger.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.model.Metric;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-09-29T07:31:44.348Z")

@Api(value = "metrics", description = "the metrics API")
public interface MetricsApi {

    @ApiOperation(value = "", notes = "Returns all the available metrics", response = Metric.class, responseContainer = "List", tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "all metrics", response = Metric.class),
        @ApiResponse(code = 200, message = "unexpected error", response = Metric.class) })
    @RequestMapping(value = "/metrics",
        produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<Metric>> findAllMetrics();

}
