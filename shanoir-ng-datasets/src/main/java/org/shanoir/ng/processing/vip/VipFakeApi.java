package org.shanoir.ng.processing.vip;

import java.io.IOException;
import java.util.List;

import org.shanoir.ng.processing.carmin.model.Execution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.annotations.ApiParam;

/**
 * Please do not use this API in production
 * This API is here to fake a VIP call to list processing and result of a processing.
 * @author jcome
 *
 */
@RequestMapping("/fakevip")
public interface VipFakeApi {
	
    @GetMapping(value = "/pipelines", produces = { "application/json" }, consumes = {
                    "application/json" })
	public ResponseEntity<List<Pipeline>> getProcessings() throws JsonMappingException, JsonProcessingException;
    
    @GetMapping(value = "/pipelines/{pipelineId}", produces = { "application/json" }, consumes = {
    "application/json" })
    public ResponseEntity<Pipeline> getProcessing(@ApiParam(value = "id of the pipeline", required = true) @PathVariable("pipelineId") String pipelineId) throws JsonMappingException, JsonProcessingException;

    @PostMapping(value = "/executions", produces = { "application/json" }, consumes = {
    "application/json" })
	public ResponseEntity<Execution> createExecution(@ApiParam(value = "execution to create", required = true) @RequestBody Execution execution);

    @GetMapping(value = "/executions/{identifier}/summary", produces = { "application/json" }, consumes = {
    "application/json" })
	public ResponseEntity<Execution> getExecution(@ApiParam(value = "identifier of the execution", required = true) @PathVariable("identifier") String identifier) throws IOException;

}
