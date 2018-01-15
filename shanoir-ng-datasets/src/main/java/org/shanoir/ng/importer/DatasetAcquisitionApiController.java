package org.shanoir.ng.importer;

import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.shared.exception.ErrorModel;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.ApiParam;

import java.util.List;

import javax.validation.constraints.*;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-01-09T09:20:01.478Z")

@Controller
public class DatasetAcquisitionApiController implements DatasetAcquisitionApi {

    public ResponseEntity<Void> createNewDatasetAcquisition(@ApiParam(value = "DatasetAcquisition to create" ,required=true )  @Valid @RequestBody ImportJob importJob) {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
