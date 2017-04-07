package org.shanoir.ng.subject;



import io.swagger.annotations.*;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-04-04T07:39:10.771Z")

@Api(value = "subject", description = "the subject API")
public interface SubjectApi {

    @ApiOperation(value = "", notes = "Deletes a subject", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "subject deleted", response = Void.class),
        @ApiResponse(code = 204, message = "no subject found", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
    @RequestMapping(value = "/subject/{subjectId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteSubject(@ApiParam(value = "id of the subject",required=true ) @PathVariable("subjectId") Long subjectId);


    @ApiOperation(value = "", notes = "Returns all the subjects", response = Subject.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found subjects", response = Subject.class),
        @ApiResponse(code = 204, message = "no subject found", response = Subject.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
        @ApiResponse(code = 403, message = "forbidden", response = Subject.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
    @RequestMapping(value = "/subject/all",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Subject>> findSubjects();


    @ApiOperation(value = "", notes = "If exists, returns the subject corresponding to the given id", response = Subject.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found bubject", response = Subject.class),
        @ApiResponse(code = 204, message = "no subject found", response = Subject.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
        @ApiResponse(code = 403, message = "forbidden", response = Subject.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
    @RequestMapping(value = "/subject/{subjectId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Subject> findSubjectById(@ApiParam(value = "id of the subject",required=true ) @PathVariable("subjectId") Long subjectId);


    @ApiOperation(value = "", notes = "Saves a new subject", response = Subject.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "created subject", response = Subject.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
        @ApiResponse(code = 403, message = "forbidden", response = Subject.class),
        @ApiResponse(code = 422, message = "bad parameters", response = Subject.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
    @RequestMapping(value = "/subject",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Subject> saveNewSubject(@ApiParam(value = "subject to create" ,required=true ) @RequestBody Subject subject, final BindingResult result)throws RestServiceException;


    @ApiOperation(value = "", notes = "Updates a subject", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "subject updated", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Void.class) })
    @RequestMapping(value = "/subject/{subjectId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updateSubject(@ApiParam(value = "id of the subject",required=true ) @PathVariable("subjectId") Long subjectId,
        @ApiParam(value = "subject to update" ,required=true ) @RequestBody Subject subject, final BindingResult result)throws RestServiceException ;
    
    @ApiOperation(value = "", notes = "If exists, returns the subjects of a study", response = Subject.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found subjects", response = Subject.class),
        @ApiResponse(code = 204, message = "no subject found", response = Subject.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Subject.class),
        @ApiResponse(code = 403, message = "forbidden", response = Subject.class),
        @ApiResponse(code = 500, message = "unexpected error", response = Subject.class) })
    @RequestMapping(value = "/subject/{studyId}/allSubjects",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Subject>> findSubjectsByStudyId(@ApiParam(value = "id of the study",required=true ) @PathVariable("studyId") Long studyId);
    
}
