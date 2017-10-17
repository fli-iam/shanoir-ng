package org.shanoir.ng.examination;


import io.swagger.annotations.*;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.List;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-10-13T07:28:38.458Z")

@Api(value = "examination", description = "the examination API")
public interface ExaminationApi {

    @ApiOperation(value = "", notes = "Deletes an examination", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "examination deleted", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "no examination found", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    
    @RequestMapping(value = "/examination/{examinationId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteExamination(@ApiParam(value = "id of the examination",required=true ) @PathVariable("examinationId") Long examinationId);


    @ApiOperation(value = "", notes = "If exists, returns the examination corresponding to the given id", response = Examination.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found examination", response = Examination.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "no examination found", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    
    @RequestMapping(value = "/examination/{examinationId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<Examination> findExaminationById(@ApiParam(value = "id of the examination",required=true ) @PathVariable("examinationId") Long examinationId);


    @ApiOperation(value = "", notes = "Returns all the examinations", response = Examination.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "found examinations", response = Examination.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "no examination found", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    
    @RequestMapping(value = "/examination/all",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Examination>> findExamination();


    @ApiOperation(value = "", notes = "Saves a new examination", response = Examination.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "created examination", response = Examination.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    
    @RequestMapping(value = "/examination",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Examination> saveNewExamination(@ApiParam(value = "examination to create" ,required=true )  @Valid @RequestBody Examination examination, final BindingResult result)throws RestServiceException;


    @ApiOperation(value = "", notes = "Updates an examination", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "examination updated", response = Void.class),
        @ApiResponse(code = 401, message = "unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "forbidden", response = Void.class),
        @ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
        @ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
    
    @RequestMapping(value = "/examination/{examinationId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<Void> updateExamination(@ApiParam(value = "id of the examination",required=true ) @PathVariable("examinationId") Long examinationId,@ApiParam(value = "examination to update" ,required=true )  @Valid @RequestBody Examination examination , final BindingResult result)throws RestServiceException;

}
