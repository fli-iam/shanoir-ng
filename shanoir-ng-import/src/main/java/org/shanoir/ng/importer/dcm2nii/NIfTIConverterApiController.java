/**
 * 
 */
package org.shanoir.ng.importer.dcm2nii;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.annotations.ApiParam;

/**
 * @author yyao
 *
 */
@Controller
public class NIfTIConverterApiController implements NIfTIConverterApi{
	
	@Autowired
	private NIfTIConverterService niftiConverterService;
	
	@Override
	public ResponseEntity<NIfTIConverter> findNiftiConverterById(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("niftiConverterId") Long niftiConverterId) {
		final NIfTIConverter niftiConverter = niftiConverterService.findById(niftiConverterId);
		if (niftiConverterId == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(niftiConverter, HttpStatus.OK);
	}

}
