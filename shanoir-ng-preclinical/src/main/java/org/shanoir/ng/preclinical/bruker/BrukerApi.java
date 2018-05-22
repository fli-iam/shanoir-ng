package org.shanoir.ng.preclinical.bruker;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "bruker", description = "the bruker API")
public interface BrukerApi {

	@ApiOperation(value = "Upload bruker zip archive file", notes = "", response = String.class, tags = {
			"BrukerModel", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "success returns ", response = String.class),
			@ApiResponse(code = 400, message = "Invalid input / Bad Request", response = Void.class),
			@ApiResponse(code = 406, message = "Not valid bruker file", response = Void.class),
			@ApiResponse(code = 500, message = "Unexpected Error", response = Void.class) })
	@RequestMapping(value = "/bruker/upload", produces = { "application/json" }, consumes = {
			MediaType.MULTIPART_FORM_DATA_VALUE, "application/json" }, method = RequestMethod.POST)
	ResponseEntity<String> uploadBrukerFile(@RequestParam("files") MultipartFile[] uploadfiles)
			throws RestServiceException;

}
