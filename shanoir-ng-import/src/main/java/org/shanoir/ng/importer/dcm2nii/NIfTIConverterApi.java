/**
 * 
 */
package org.shanoir.ng.importer.dcm2nii;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author yyao
 *
 */
@Api(value = "nifticonverter", description = "the niftiConverter API")
@RequestMapping("/niftiConverter")
public interface NIfTIConverterApi {
	@ApiOperation(value = "", notes = "If exists, returns the niftiConverter corresponding to the given id", response = NIfTIConverter.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "found nifti converter", response = NIfTIConverter.class),
			@ApiResponse(code = 401, message = "unauthorized", response = NIfTIConverter.class),
			@ApiResponse(code = 403, message = "forbidden", response = NIfTIConverter.class),
			@ApiResponse(code = 404, message = "no nifti converter found", response = NIfTIConverter.class),
			@ApiResponse(code = 500, message = "unexpected error", response = NIfTIConverter.class) })
	@RequestMapping(value = "/{niftiConverterId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<NIfTIConverter> findNiftiConverterById(
			@ApiParam(value = "id of the niftiConverter", required = true) @PathVariable("niftiConverterId") Long niftiConverterId);
}
