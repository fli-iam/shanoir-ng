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

	public void importAsBids(File bidsFile) {
		throw new NotImplementedException("This method was not implemented yet");
		
		// Options
		// CREATE_SUBJECT
		// DO_NOT_IMPORT_DATA (only modify BIDS folder?)
		//
		
//		1) Unzip and check elements
//		1.1) Unzip
//		1.2) Serialize as BidsElement
//		1.3) Validate Bids format
//		1.4) Check that the study exists
//		1.5) Generate Study BIDS folder if not existing
//
//		2) Import Datasets
//		2.1) Iterate over subjects folders
//		2.1.1) Create subject if not existing
//		2.2) Iterate over Examination folders
//		2.2.1) Check that center / acquisition_equipement exists
//		2.2.2) import.uploadXXXZipFile()
//		2.2.3) create examination, select center, subject, study, etc..
//		2.2.4) import.startImportJob
//		Datasets are now created and added to BIDS folder using addDataset
//
		// 4. Copy BIDS folder
		// Copy non datasets elements
		// Don't copy "data" folder
		// Don't copy examination_description.json
		// copy /sourceData, /code and / files
	}
}