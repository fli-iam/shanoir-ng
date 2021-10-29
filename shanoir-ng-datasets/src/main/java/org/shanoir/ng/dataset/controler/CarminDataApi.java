package org.shanoir.ng.dataset.controler;

import io.swagger.annotations.*;
import org.shanoir.ng.dataset.model.carmin.GetPathResponse;
import org.shanoir.ng.dataset.model.carmin.Path;
import org.shanoir.ng.dataset.model.carmin.UploadData;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@Api(value = "carmin-data")
public interface CarminDataApi {


    @ApiOperation(value = "Delete a path", notes = "Delete a path and transitively delete all its content if it is a directory.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "the deletion is successful and finished."),

            @ApiResponse(code = 200, message = "A functional or internal error occured processing the request") })
    @RequestMapping(value = "/path/{completePath}",
            produces = { "application/json" },
            method = RequestMethod.DELETE)
    ResponseEntity<Void> deletePath(@ApiParam(value = "The complete path to delete. It can contain non-encoded slashes.", required=true) @PathVariable("completePath") String completePath);


    @ApiOperation(value = "Get content or information for a given path", notes = "Download a file (or a directory) or retun information about a specific path. The response format and content depends on the mandatory action query parameter (see the parameter description). Basically, the \"content\" action downloads the raw file, and the other actions return various information in a JSON record.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful response. If the action is \"content\", the raw file (or a tarball) is returned, with the according mime type. Otherwise a json response a returned", response = GetPathResponse.class),

            @ApiResponse(code = 200, message = "A functional or internal error occured processing the request") })
    @RequestMapping(value = "/path/{completePath}",
            produces = { "application/json", "application/octet-stream" },
            method = RequestMethod.GET)
    ResponseEntity<?> getPath(@ApiParam(value = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error", required=true) @PathVariable("completePath") String completePath, @NotNull @ApiParam(value = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition)." ,required=true
    ) @Valid @RequestParam(value = "action", required = true) String action, HttpServletResponse response) throws IOException, RestServiceException;


    @ApiOperation(value = "Upload data to a path", tags = "A request without content creates a directory (an error should be returned if the path already exists). A request with a specific content type (\"application/carmin+json\") allows to upload data encoded in base64. The base64 content (part of a json payload) can either be an encoded file, are an encoded zip archive that will create a directory. All other content (with any content type) will be considered as a raw file and will override the existing path content. If the parent directory of the file/directory to create does not exist, an error must be returned.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The upload is successful and finished.", response = Path.class),

            @ApiResponse(code = 200, message = "A functional or internal error occured processing the request") })
    @RequestMapping(value = "/path/{completePath}",
            produces = { "application/json" },
            consumes = { "application/carmin+json", "application/octet-stream" },
            method = RequestMethod.PUT)
    ResponseEntity<Path> uploadPath(@ApiParam(value = "The complete path on which to upload data. It can contain non-encoded slashes.", required=true) @PathVariable("completePath") String completePath, @ApiParam(value = "") @Valid @RequestBody UploadData body);

}
