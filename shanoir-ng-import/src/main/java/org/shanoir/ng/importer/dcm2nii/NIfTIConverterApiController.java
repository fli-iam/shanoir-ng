/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

/**
 * 
 */
package org.shanoir.ng.importer.dcm2nii;

import java.util.List;

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
	private DatasetsCreatorAndNIfTIConverterService niftiConverterService;
	
	@Override
	public ResponseEntity<NIfTIConverter> findNiftiConverterById(
			@ApiParam(value = "id of the study card", required = true) @PathVariable("niftiConverterId") Long niftiConverterId) {
		final NIfTIConverter niftiConverter = niftiConverterService.findById(niftiConverterId);
		if (niftiConverterId == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(niftiConverter, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<NIfTIConverter>> findNiftiConverters() {
		final List<NIfTIConverter> niftiConverters = niftiConverterService.findAll();
		if (niftiConverters.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(niftiConverters, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Boolean> convertData(Long niftiConverterId, String dataPath) {
		
		return null;
	}
}
