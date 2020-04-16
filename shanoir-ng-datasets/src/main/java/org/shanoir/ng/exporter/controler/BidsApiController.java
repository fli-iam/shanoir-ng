package org.shanoir.ng.exporter.controler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.NotImplementedException;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.annotations.ApiParam;

@Controller
public class BidsApiController implements BidsApi {

	@Autowired
	BIDSService bidsService;

	@Override
	public ResponseEntity<Void> generateBIDSByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "name of the study", required=true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException {
		bidsService.exportAsBids(studyId, studyName);
		return ResponseEntity.ok().build();
	}
}