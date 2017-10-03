package org.shanoir.ng.importer.examination;

import java.util.List;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-11-18T15:36:13.002Z")

@Api(value = "template", description = "the template API")
@RequestMapping("/template")
public interface TemplateApi {

	@ApiOperation(value = "", notes = "Deletes a template", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "template deleted", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 404, message = "no template found", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{templateId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('adminRole')")
	ResponseEntity<Void> deleteTemplate(
			@ApiParam(value = "id of the template", required = true) @PathVariable("templateId") Long templateId);

	@ApiOperation(value = "", notes = "If exists, returns the template corresponding to the given id", response = Template.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found template", response = Template.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Template.class),
			@ApiResponse(code = 403, message = "forbidden", response = Template.class),
			@ApiResponse(code = 404, message = "no template found", response = Template.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Template.class) })
	@RequestMapping(value = "/{templateId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Template> findTemplateById(
			@ApiParam(value = "id of the template", required = true) @PathVariable("templateId") Long templateId);

	@ApiOperation(value = "", notes = "Returns all the templates", response = Template.class, responseContainer = "List", tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found templates", response = Template.class),
			@ApiResponse(code = 204, message = "no template found", response = Template.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Template.class),
			@ApiResponse(code = 403, message = "forbidden", response = Template.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Template.class) })
	@RequestMapping(value = "/all", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<Template>> findTemplates();

	@ApiOperation(value = "", notes = "Saves a new template", response = Template.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created template", response = Template.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Template.class),
			@ApiResponse(code = 403, message = "forbidden", response = Template.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Template.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Template.class) })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Template> saveNewTemplate(@ApiParam(value = "template to create", required = true) @RequestBody Template template,
			BindingResult result) throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a template", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "template updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/{templateId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	ResponseEntity<Void> updateTemplate(
			@ApiParam(value = "id of the template", required = true) @PathVariable("templateId") Long templateId,
			@ApiParam(value = "template to update", required = true) @RequestBody Template template, BindingResult result)
			throws RestServiceException;

}
