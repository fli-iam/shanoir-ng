package org.shanoir.ng.study;

import org.shanoir.ng.shared.exception.ErrorModel;

import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T10:35:29.288Z")

@Api(value = "study", description = "the study API")

public interface StudyApi {

    @ApiOperation(value = "", notes = "Deletes a study", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "study deleted", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "no study found", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
    @RequestMapping(value = "/study/{studyId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteStudy(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId);


    @ApiOperation(value = "", notes = "Returns all the studies", response = Study.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found studies", response = Study.class),
        @ApiResponse(code = 204, message = "no study found", response = Study.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Study.class),
        @ApiResponse(code = 403, message = "forbidden", response = Study.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
    @RequestMapping(value = "/study/all",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Study>> findStudies();


    @ApiOperation(value = "", notes = "If exists, returns the studies that the user is allowed to see", response = Study.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found studies", response = Study.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Study.class),
        @ApiResponse(code = 403, message = "forbidden", response = Study.class),
        @ApiResponse(code = 404, message = "no study found", response = Study.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
    @RequestMapping(value = "/study/{userId}/allStudies",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Study>> findStudiesByUserId(@ApiParam(value = "id of the user",required=true ) @PathVariable("userId") Long userId);


    @ApiOperation(value = "", notes = "If exists, returns the study corresponding to the given id", response = Study.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found study", response = Study.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Study.class),
        @ApiResponse(code = 403, message = "forbidden", response = Study.class),
        @ApiResponse(code = 404, message = "no study found", response = Study.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
    @RequestMapping(value = "/study/{studyId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Study> findStudyById(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId);


    @ApiOperation(value = "", notes = "Saves a new study", response = Study.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "created study", response = Study.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Study.class),
        @ApiResponse(code = 403, message = "forbidden", response = Study.class),
        @ApiResponse(code = 422, message = "bad parameters", response = Study.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Study.class) })
    @RequestMapping(value = "/study",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Study> saveNewStudy(@ApiParam(value = "study to create" ,required=true ) @RequestBody Study study);


    @ApiOperation(value = "", notes = "Updates a study", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "study updated", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
    @RequestMapping(value = "/study/{studyId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updateStudy(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId,
        @ApiParam(value = "study to update" ,required=true ) @RequestBody Study study);


}
